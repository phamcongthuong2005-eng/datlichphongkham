package com.phongkham.app.activities.doctor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class DoctorMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DoctorDao doctorDao;
    private AppointmentDao appointmentDao;
    private AuditLogDao auditLogDao;

    private TextView tvDoctorHeaderName, tvDoctorHeaderSpecialty, tvAppointmentCount, tvDoctorEmptyState;
    private ImageView ivDoctorLogout;
    private Button btnDoctorSchedule;
    private EditText etDoctorDateFilter;
    private RecyclerView rvDoctorAppointments;

    private Doctor currentDoctor;
    private String selectedDate = "";
    private List<Appointment> appointmentList = new ArrayList<>();
    private AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        sessionManager = new SessionManager(this);
        doctorDao = new DoctorDao(this);
        appointmentDao = new AppointmentDao(this);
        auditLogDao = new AuditLogDao(this);

        selectedDate = DateTimeUtils.getToday();

        initViews();
        loadDoctorProfile();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        tvDoctorHeaderName = findViewById(R.id.tvDoctorHeaderName);
        tvDoctorHeaderSpecialty = findViewById(R.id.tvDoctorHeaderSpecialty);
        tvAppointmentCount = findViewById(R.id.tvAppointmentCount);
        tvDoctorEmptyState = findViewById(R.id.tvDoctorEmptyState);
        ivDoctorLogout = findViewById(R.id.ivDoctorLogout);
        btnDoctorSchedule = findViewById(R.id.btnDoctorSchedule);
        etDoctorDateFilter = findViewById(R.id.etDoctorDateFilter);
        rvDoctorAppointments = findViewById(R.id.rvDoctorAppointments);

        etDoctorDateFilter.setText(selectedDate);
        rvDoctorAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadDoctorProfile() {
        int userId = sessionManager.getUserId();
        currentDoctor = doctorDao.getDoctorByUserId(userId);
        if (currentDoctor == null) {
            // Fallback first doctor
            List<Doctor> all = doctorDao.getAllDoctors();
            if (!all.isEmpty()) currentDoctor = all.get(0);
        }

        if (currentDoctor != null) {
            tvDoctorHeaderName.setText(currentDoctor.getFullName());
            tvDoctorHeaderSpecialty.setText("Chuyên khoa " + currentDoctor.getSpecialtyName() + " (" + currentDoctor.getDegree() + ")");
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
        tvAppointmentCount.setText("Danh sách bệnh nhân hẹn khám (" + appointmentList.size() + ")");

        if (appointmentList.isEmpty()) {
            tvDoctorEmptyState.setVisibility(View.VISIBLE);
            rvDoctorAppointments.setVisibility(View.GONE);
        } else {
            tvDoctorEmptyState.setVisibility(View.GONE);
            rvDoctorAppointments.setVisibility(View.VISIBLE);

            adapter = new AppointmentAdapter(this, appointmentList, User.ROLE_DOCTOR, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    // Bắt đầu khám / Tiếp tục khám / Xem hồ sơ
                    Intent intent = new Intent(DoctorMainActivity.this, DoctorExamineActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivity(intent);
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {
                    // Báo vắng mặt
                    new AlertDialog.Builder(DoctorMainActivity.this)
                            .setTitle("Cập nhật vắng mặt")
                            .setMessage("Đánh dấu bệnh nhân " + appointment.getPatientName() + " không đến khám (No-Show)?")
                            .setPositiveButton("Xác nhận vắng", (dialog, which) -> {
                                appointmentDao.updateStatus(appointment.getId(), Appointment.STATUS_NOSHOW);
                                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                                        "CẬP NHẬT TRẠNG THÁI", "Đánh dấu bệnh nhân vắng mặt cho lịch " + appointment.getAppointmentCode());
                                Toast.makeText(DoctorMainActivity.this, "Đã cập nhật trạng thái vắng mặt", Toast.LENGTH_SHORT).show();
                                loadAppointments();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }

                @Override
                public void onItemClick(Appointment appointment) {
                    Intent intent = new Intent(DoctorMainActivity.this, DoctorExamineActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivity(intent);
                }
            });
            rvDoctorAppointments.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        btnDoctorSchedule.setOnClickListener(v -> {
            if (currentDoctor != null) {
                Intent intent = new Intent(DoctorMainActivity.this, DoctorScheduleActivity.class);
                intent.putExtra("doctor", currentDoctor);
                startActivity(intent);
            }
        });

        ivDoctorLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản Bác sĩ?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(DoctorMainActivity.this, AuthActivity.class));
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
