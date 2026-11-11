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

public class StaffMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AppointmentDao appointmentDao;
    private AuditLogDao auditLogDao;

    private TextView tvStaffHeaderName, tvStaffQueueTitle, tvStaffEmptyState;
    private ImageView ivStaffLogout;
    private Button btnStaffCreateBooking;
    private EditText etStaffSearch;
    private RecyclerView rvStaffAppointments;

    private List<Appointment> allList = new ArrayList<>();
    private List<Appointment> displayList = new ArrayList<>();
    private AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);

        sessionManager = new SessionManager(this);
        appointmentDao = new AppointmentDao(this);
        auditLogDao = new AuditLogDao(this);

        initViews();
        loadAppointments();
        setupListeners();
    }

    private void initViews() {
        tvStaffHeaderName = findViewById(R.id.tvStaffHeaderName);
        tvStaffQueueTitle = findViewById(R.id.tvStaffQueueTitle);
        tvStaffEmptyState = findViewById(R.id.tvStaffEmptyState);
        ivStaffLogout = findViewById(R.id.ivStaffLogout);
        btnStaffCreateBooking = findViewById(R.id.btnStaffCreateBooking);
        etStaffSearch = findViewById(R.id.etStaffSearch);
        rvStaffAppointments = findViewById(R.id.rvStaffAppointments);

        tvStaffHeaderName.setText("Lễ Tân: " + sessionManager.getFullName());
        rvStaffAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAppointments() {
        allList = appointmentDao.getAppointmentsByDate(DateTimeUtils.getToday());
        filterAppointments();
    }

    private void filterAppointments() {
        String keyword = etStaffSearch.getText().toString().trim().toLowerCase();
        displayList.clear();

        for (Appointment a : allList) {
            if (keyword.isEmpty() ||
                    a.getPatientName().toLowerCase().contains(keyword) ||
                    a.getPatientPhone().toLowerCase().contains(keyword) ||
                    a.getAppointmentCode().toLowerCase().contains(keyword) ||
                    a.getDoctorName().toLowerCase().contains(keyword)) {
                displayList.add(a);
            }
        }

        tvStaffQueueTitle.setText("Danh Sách Tiếp Nhận & Hàng Đợi Trong Ngày (" + displayList.size() + ")");

        if (displayList.isEmpty()) {
            tvStaffEmptyState.setVisibility(View.VISIBLE);
            rvStaffAppointments.setVisibility(View.GONE);
        } else {
            tvStaffEmptyState.setVisibility(View.GONE);
            rvStaffAppointments.setVisibility(View.VISIBLE);

            adapter = new AppointmentAdapter(this, displayList, User.ROLE_STAFF, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    if (Appointment.STATUS_PENDING.equals(appointment.getStatus())) {
                        // Check in and assign Queue Number
                        handleCheckInPatient(appointment);
                    } else if (Appointment.STATUS_COMPLETED.equals(appointment.getStatus())) {
                        // Lập hóa đơn viện phí
                        Intent intent = new Intent(StaffMainActivity.this, StaffInvoiceActivity.class);
                        intent.putExtra("appointment", appointment);
                        startActivity(intent);
                    } else {
                        showDetailDialog(appointment);
                    }
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {
                    // Hủy lịch
                    handleCancelAppointment(appointment);
                }

                @Override
                public void onItemClick(Appointment appointment) {
                    showDetailDialog(appointment);
                }
            });
            rvStaffAppointments.setAdapter(adapter);
        }
    }

    private void handleCheckInPatient(Appointment appt) {
        int nextQueue = appointmentDao.getNextQueueNumber(appt.getAppointmentDate());

        new AlertDialog.Builder(this)
                .setTitle("Tiếp nhận bệnh nhân đến phòng khám")
                .setMessage("Xác nhận bệnh nhân " + appt.getPatientName() + " đã có mặt tại quầy?\n\nCấp Số Thứ Tự Khám: #" + nextQueue)
                .setPositiveButton("Cấp số #" + nextQueue, (dialog, which) -> {
                    appointmentDao.assignQueueNumber(appt.getId(), nextQueue);
                    auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_STAFF,
                            "TIẾP NHẬN BỆNH NHÂN", "Lễ tân đã tiếp nhận bệnh nhân " + appt.getPatientName() + " và cấp STT #" + nextQueue);
                    Toast.makeText(StaffMainActivity.this, "Đã tiếp nhận thành công! STT khám: #" + nextQueue, Toast.LENGTH_LONG).show();
                    loadAppointments();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleCancelAppointment(Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy lịch khám")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn mã " + appt.getAppointmentCode() + "?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    appointmentDao.updateStatus(appt.getId(), Appointment.STATUS_CANCELLED);
                    auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_STAFF,
                            "HỦY LỊCH HẸN", "Nhân viên đã hủy lịch " + appt.getAppointmentCode());
                    Toast.makeText(StaffMainActivity.this, "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showDetailDialog(Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Thông tin lịch khám: " + appt.getAppointmentCode())
                .setMessage("• Bệnh nhân: " + appt.getPatientName() + " (SĐT: " + appt.getPatientPhone() + ")\n" +
                        "• Bác sĩ: " + appt.getDoctorName() + " (" + appt.getSpecialtyName() + ")\n" +
                        "• Ngày khám: " + appt.getAppointmentDate() + "\n" +
                        "• Khung giờ: " + appt.getTimeSlot() + "\n" +
                        "• Trạng thái: " + appt.getStatus() + "\n" +
                        "• Số thứ tự: #" + (appt.getQueueNumber() > 0 ? appt.getQueueNumber() : "Chưa cấp") + "\n" +
                        "• Lý do khám: " + appt.getSymptoms())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void setupListeners() {
        btnStaffCreateBooking.setOnClickListener(v -> startActivity(new Intent(this, StaffCreateBookingActivity.class)));

        etStaffSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAppointments();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivStaffLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản Lễ tân?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(StaffMainActivity.this, AuthActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }
}
