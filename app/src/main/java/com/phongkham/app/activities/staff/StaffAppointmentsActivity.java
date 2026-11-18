package com.phongkham.app.activities.staff;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.AppointmentAdapter;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StaffAppointmentsActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AppointmentDao appointmentDao;
    private AuditLogDao auditLogDao;

    private ImageView ivBack;
    private EditText etStaffSearch, etStaffDateFilter;
    private TextView tvStaffQueueTitle, tvStaffTotalCount, tvStaffEmptyState;
    private RecyclerView rvStaffAppointments;

    private String selectedDate = "";
    private List<Appointment> fullAppointmentList = new ArrayList<>();
    private List<Appointment> displayList = new ArrayList<>();
    private AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_appointments);

        sessionManager = new SessionManager(this);
        appointmentDao = new AppointmentDao(this);
        auditLogDao = new AuditLogDao(this);

        selectedDate = DateTimeUtils.getToday();

        initViews();
        setupDatePicker();
        setupSearchListener();
        loadAppointments();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etStaffSearch = findViewById(R.id.etStaffSearch);
        etStaffDateFilter = findViewById(R.id.etStaffDateFilter);
        tvStaffQueueTitle = findViewById(R.id.tvStaffQueueTitle);
        tvStaffTotalCount = findViewById(R.id.tvStaffTotalCount);
        tvStaffEmptyState = findViewById(R.id.tvStaffEmptyState);
        rvStaffAppointments = findViewById(R.id.rvStaffAppointments);

        ivBack.setOnClickListener(v -> finish());
        etStaffDateFilter.setText(selectedDate);
        rvStaffAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDatePicker() {
        etStaffDateFilter.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(cal.getTime());
                etStaffDateFilter.setText(selectedDate);
                loadAppointments();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void setupSearchListener() {
        etStaffSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAppointments(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAppointments() {
        fullAppointmentList = appointmentDao.getAppointmentsByDate(selectedDate);
        filterAppointments(etStaffSearch.getText().toString().trim());
    }

    private void filterAppointments(String query) {
        displayList.clear();
        for (Appointment a : fullAppointmentList) {
            boolean match = query.isEmpty() ||
                    (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(query.toLowerCase())) ||
                    (a.getPatientPhone() != null && a.getPatientPhone().contains(query)) ||
                    (a.getAppointmentCode() != null && a.getAppointmentCode().toLowerCase().contains(query.toLowerCase()));
            if (match) {
                displayList.add(a);
            }
        }

        tvStaffTotalCount.setText("Tổng số: " + displayList.size() + " ca");
        if (displayList.isEmpty()) {
            rvStaffAppointments.setVisibility(View.GONE);
            tvStaffEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvStaffAppointments.setVisibility(View.VISIBLE);
            tvStaffEmptyState.setVisibility(View.GONE);

            adapter = new AppointmentAdapter(this, displayList, User.ROLE_STAFF, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    if (Appointment.STATUS_PENDING.equals(appointment.getStatus())) {
                        showCheckInDialog(appointment);
                    } else if (Appointment.STATUS_COMPLETED.equals(appointment.getStatus())) {
                        Intent intent = new Intent(StaffAppointmentsActivity.this, StaffInvoiceActivity.class);
                        intent.putExtra("appointment", appointment);
                        startActivity(intent);
                    } else {
                        showCheckInDialog(appointment);
                    }
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {
                    showCancelDialog(appointment);
                }

                @Override
                public void onItemClick(Appointment appointment) {
                    if (Appointment.STATUS_COMPLETED.equals(appointment.getStatus())) {
                        Intent intent = new Intent(StaffAppointmentsActivity.this, StaffInvoiceActivity.class);
                        intent.putExtra("appointment", appointment);
                        startActivity(intent);
                    } else {
                        showCheckInDialog(appointment);
                    }
                }
            });
            rvStaffAppointments.setAdapter(adapter);
        }
    }

    private void showCheckInDialog(Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Tiếp nhận & Check-in")
                .setMessage("Xác nhận bệnh nhân " + appt.getPatientName() + " đã đến phòng khám và cấp số thứ tự vào hàng chờ?")
                .setPositiveButton("Check-in ngay", (dialog, which) -> {
                    int nextQueue = appointmentDao.checkInAppointment(appt.getId());
                    auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_STAFF,
                            "CHECK-IN BỆNH NHÂN", "Check-in thành công cho " + appt.getPatientName() + " - STT khám: " + nextQueue);
                    Toast.makeText(this, "Check-in thành công! Số thứ tự khám: STT " + nextQueue, Toast.LENGTH_LONG).show();
                    loadAppointments();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCancelDialog(Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối / Hủy lịch hẹn")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn của bệnh nhân " + appt.getPatientName() + "?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    appointmentDao.updateStatus(appt.getId(), Appointment.STATUS_CANCELLED, "Lễ tân từ chối/hủy");
                    auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_STAFF,
                            "HỦY LỊCH HẸN", "Lễ tân hủy lịch hẹn mã " + appt.getAppointmentCode());
                    Toast.makeText(this, "Đã hủy lịch hẹn thành công!", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }
}
