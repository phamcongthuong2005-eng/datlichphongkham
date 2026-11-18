package com.phongkham.app.activities.doctor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DoctorAppointmentsActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DoctorDao doctorDao;
    private AppointmentDao appointmentDao;
    private AuditLogDao auditLogDao;

    private ImageView ivBack;
    private TextView tvAppointmentCount, tvDoctorEmptyState;
    private EditText etDoctorDateFilter;
    private RecyclerView rvDoctorAppointments;

    private Doctor currentDoctor;
    private String selectedDate = "";
    private List<Appointment> appointmentList = new ArrayList<>();
    private AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        sessionManager = new SessionManager(this);
        doctorDao = new DoctorDao(this);
        appointmentDao = new AppointmentDao(this);
        auditLogDao = new AuditLogDao(this);

        selectedDate = DateTimeUtils.getToday();

        initViews();
        loadDoctorProfile();
        setupDatePicker();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvAppointmentCount = findViewById(R.id.tvAppointmentCount);
        tvDoctorEmptyState = findViewById(R.id.tvDoctorEmptyState);
        etDoctorDateFilter = findViewById(R.id.etDoctorDateFilter);
        rvDoctorAppointments = findViewById(R.id.rvDoctorAppointments);

        ivBack.setOnClickListener(v -> finish());
        etDoctorDateFilter.setText(selectedDate);
        rvDoctorAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadDoctorProfile() {
        int userId = sessionManager.getUserId();
        currentDoctor = doctorDao.getDoctorByUserId(userId);
        if (currentDoctor == null) {
            List<Doctor> all = doctorDao.getAllDoctors();
            if (!all.isEmpty()) currentDoctor = all.get(0);
        }
        loadAppointments();
    }

    private void setupDatePicker() {
        etDoctorDateFilter.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(cal.getTime());
                etDoctorDateFilter.setText(selectedDate);
                loadAppointments();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void loadAppointments() {
        if (currentDoctor == null) return;
        appointmentList = appointmentDao.getAppointmentsByDoctorAndDate(currentDoctor.getId(), selectedDate);

        if (appointmentList.isEmpty()) {
            rvDoctorAppointments.setVisibility(View.GONE);
            tvDoctorEmptyState.setVisibility(View.VISIBLE);
            tvAppointmentCount.setText("Danh sách ca khám ngày " + selectedDate + " (0 ca):");
        } else {
            rvDoctorAppointments.setVisibility(View.VISIBLE);
            tvDoctorEmptyState.setVisibility(View.GONE);
            tvAppointmentCount.setText("Danh sách ca khám ngày " + selectedDate + " (" + appointmentList.size() + " bệnh nhân):");

            adapter = new AppointmentAdapter(this, appointmentList, User.ROLE_DOCTOR, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    Intent intent = new Intent(DoctorAppointmentsActivity.this, DoctorExamineActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivity(intent);
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {
                    showCancelDialog(appointment);
                }

                @Override
                public void onItemClick(Appointment appointment) {
                    Intent intent = new Intent(DoctorAppointmentsActivity.this, DoctorExamineActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivity(intent);
                }
            });
            rvDoctorAppointments.setAdapter(adapter);
        }
    }

    private void showCancelDialog(Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy ca khám")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn của bệnh nhân " + appt.getPatientName() + "?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    appointmentDao.updateStatus(appt.getId(), Appointment.STATUS_CANCELLED, "Bác sĩ hủy vì bận đột xuất");
                    auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                            "HỦY LỊCH HẸN", "Bác sĩ hủy lịch hẹn mã " + appt.getAppointmentCode());
                    Toast.makeText(this, "Đã hủy ca khám thành công!", Toast.LENGTH_SHORT).show();
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
