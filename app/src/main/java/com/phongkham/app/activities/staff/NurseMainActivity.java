package com.phongkham.app.activities.staff;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.activities.AuthActivity;
import com.phongkham.app.adapters.AppointmentAdapter;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NurseMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AppointmentDao appointmentDao;
    private AuditLogDao auditLogDao;

    private TextView tvNurseHeaderName, tvCurrentCallingNumber, tvCurrentCallingPatient, tvCurrentCallingRoom, tvNurseQueueTitle, tvNurseEmptyState;
    private ImageView ivNurseLogout;
    private Button btnRecallPatient, btnCallNext;
    private EditText etNurseSearch;
    private RecyclerView rvNurseQueue;

    private List<Appointment> allList = new ArrayList<>();
    private List<Appointment> displayList = new ArrayList<>();
    private AppointmentAdapter adapter;
    private Appointment currentCallingAppt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_main);

        sessionManager = new SessionManager(this);
        appointmentDao = new AppointmentDao(this);
        auditLogDao = new AuditLogDao(this);

        initViews();
        loadQueue();
        setupListeners();
    }

    private void initViews() {
        tvNurseHeaderName = findViewById(R.id.tvNurseHeaderName);
        tvCurrentCallingNumber = findViewById(R.id.tvCurrentCallingNumber);
        tvCurrentCallingPatient = findViewById(R.id.tvCurrentCallingPatient);
        tvCurrentCallingRoom = findViewById(R.id.tvCurrentCallingRoom);
        tvNurseQueueTitle = findViewById(R.id.tvNurseQueueTitle);
        tvNurseEmptyState = findViewById(R.id.tvNurseEmptyState);
        ivNurseLogout = findViewById(R.id.ivNurseLogout);
        btnRecallPatient = findViewById(R.id.btnRecallPatient);
        btnCallNext = findViewById(R.id.btnCallNext);
        etNurseSearch = findViewById(R.id.etNurseSearch);
        rvNurseQueue = findViewById(R.id.rvNurseQueue);

        tvNurseHeaderName.setText("Y Tá: " + sessionManager.getFullName());
        rvNurseQueue.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadQueue() {
        allList = appointmentDao.getAppointmentsByDate(DateTimeUtils.getToday());
        filterQueue();
    }

    private void filterQueue() {
        String keyword = etNurseSearch.getText().toString().trim().toLowerCase();
        displayList.clear();

        for (Appointment a : allList) {
            if (keyword.isEmpty() ||
                    a.getPatientName().toLowerCase().contains(keyword) ||
                    a.getPatientPhone().toLowerCase().contains(keyword) ||
                    a.getAppointmentCode().toLowerCase().contains(keyword) ||
                    String.valueOf(a.getQueueNumber()).contains(keyword)) {
                displayList.add(a);
            }
        }

        tvNurseQueueTitle.setText("Danh Sách Bệnh Nhân Đang Chờ Đo Sinh Hiệu & Khám (" + displayList.size() + ")");

        if (displayList.isEmpty()) {
            tvNurseEmptyState.setVisibility(View.VISIBLE);
            rvNurseQueue.setVisibility(View.GONE);
        } else {
            tvNurseEmptyState.setVisibility(View.GONE);
            rvNurseQueue.setVisibility(View.VISIBLE);

            adapter = new AppointmentAdapter(this, displayList, User.ROLE_NURSE, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    // Mở màn hình Đo chỉ số sinh hiệu (Menu 3.8)
                    Intent intent = new Intent(NurseMainActivity.this, NurseMeasureVitalsActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivity(intent);
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {
                    // Đánh dấu vắng mặt (Menu 3.7)
                    handleMarkNoShow(appointment);
                }

                @Override
                public void onItemClick(Appointment appointment) {
                    showCallingDialog(appointment);
                }
            });
            rvNurseQueue.setAdapter(adapter);
        }
    }

    private void showCallingDialog(Appointment appt) {
        currentCallingAppt = appt;
        tvCurrentCallingNumber.setText("#" + appt.getQueueNumber());
        tvCurrentCallingPatient.setText("Bệnh nhân: " + appt.getPatientName());
        tvCurrentCallingRoom.setText("Phòng khám: " + appt.getRoomCode() + " - " + appt.getDoctorName());

        new AlertDialog.Builder(this)
                .setTitle("📢 Gọi Bệnh Nhân Vào Phòng")
                .setMessage("• Số thứ tự: #" + appt.getQueueNumber() + "\n" +
                        "• Họ tên: " + appt.getPatientName() + " (" + appt.getPatientPhone() + ")\n" +
                        "• Phòng khám: " + appt.getRoomCode() + "\n" +
                        "• Bác sĩ: " + appt.getDoctorName() + "\n\n" +
                        "Chọn hành động tiếp theo:")
                .setPositiveButton("Đo Chỉ Số Sinh Hiệu", (dialog, which) -> {
                    Intent intent = new Intent(NurseMainActivity.this, NurseMeasureVitalsActivity.class);
                    intent.putExtra("appointment", appt);
                    startActivity(intent);
                })
                .setNegativeButton("Báo Vắng Mặt", (dialog, which) -> handleMarkNoShow(appt))
                .setNeutralButton("Đóng", null)
                .show();
    }

    private void handleMarkNoShow(Appointment appt) {
        String[] reasons = {"Không có mặt khi gọi 3 lần", "Bệnh nhân đã rời phòng khám", "Bận đột xuất", "Lý do khác"};
        new AlertDialog.Builder(this)
                .setTitle("Đánh dấu bệnh nhân vắng mặt")
                .setItems(reasons, (dialog, which) -> {
                    String reason = reasons[which];
                    appointmentDao.updateStatus(appt.getId(), Appointment.STATUS_NOSHOW);
                    auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_NURSE,
                            "ĐÁNH DẤU VẮNG MẶT", "Y tá đánh dấu vắng mặt bệnh nhân " + appt.getPatientName() + " - Lý do: " + reason);
                    Toast.makeText(this, "Đã cập nhật trạng thái vắng mặt", Toast.LENGTH_SHORT).show();
                    loadQueue();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupListeners() {
        btnCallNext.setOnClickListener(v -> {
            for (Appointment a : displayList) {
                if (Appointment.STATUS_CONFIRMED.equals(a.getStatus()) || Appointment.STATUS_WAITING.equals(a.getStatus())) {
                    showCallingDialog(a);
                    return;
                }
            }
            Toast.makeText(this, "Không còn bệnh nhân nào đang chờ tiếp theo!", Toast.LENGTH_SHORT).show();
        });

        btnRecallPatient.setOnClickListener(v -> {
            if (currentCallingAppt != null) {
                currentCallingAppt.setCallCount(currentCallingAppt.getCallCount() + 1);
                Toast.makeText(this, "🔊 Đang phát thông báo gọi lại lần " + currentCallingAppt.getCallCount() + " cho STT #" + currentCallingAppt.getQueueNumber(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Chưa có bệnh nhân nào được chọn để gọi lại!", Toast.LENGTH_SHORT).show();
            }
        });

        etNurseSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQueue();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivNurseLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản Y tá?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(NurseMainActivity.this, AuthActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQueue();
    }
}
