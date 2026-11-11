package com.phongkham.app.activities.doctor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DoctorScheduleActivity extends AppCompatActivity {
    private ImageView ivBack;
    private EditText etWorkingDays, etDoctorBio, etLeaveDate, etLeaveReason;
    private Button btnSubmitLeave, btnSaveSchedule;

    private DoctorDao doctorDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private Doctor doctor;
    private String selectedLeaveDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        doctorDao = new DoctorDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        doctor = (Doctor) getIntent().getSerializableExtra("doctor");

        initViews();
        displayData();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etWorkingDays = findViewById(R.id.etWorkingDays);
        etDoctorBio = findViewById(R.id.etDoctorBio);
        etLeaveDate = findViewById(R.id.etLeaveDate);
        etLeaveReason = findViewById(R.id.etLeaveReason);
        btnSubmitLeave = findViewById(R.id.btnSubmitLeave);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
    }

    private void displayData() {
        if (doctor != null) {
            etWorkingDays.setText(doctor.getWorkingDays());
            etDoctorBio.setText(doctor.getBio());
        }
    }

    private void setupDatePicker() {
        etLeaveDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedLeaveDate = sdf.format(cal.getTime());
                etLeaveDate.setText(selectedLeaveDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSaveSchedule.setOnClickListener(v -> {
            if (doctor == null) return;
            doctor.setWorkingDays(etWorkingDays.getText().toString().trim());
            doctor.setBio(etDoctorBio.getText().toString().trim());

            boolean ok = doctorDao.updateDoctor(doctor);
            if (ok) {
                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                        "CẬP NHẬT LỊCH LÀM VIỆC", "Bác sĩ đã cập nhật lịch trực khám: " + doctor.getWorkingDays());
                Toast.makeText(this, "Đã cập nhật lịch làm việc thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSubmitLeave.setOnClickListener(v -> {
            if (selectedLeaveDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày muốn đăng ký nghỉ!", Toast.LENGTH_SHORT).show();
                return;
            }
            String reason = etLeaveReason.getText().toString().trim();

            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đăng ký nghỉ")
                    .setMessage("Đăng ký nghỉ trực ngày " + selectedLeaveDate + " với lý do: " + (reason.isEmpty() ? "Việc cá nhân" : reason) + "?\n(Hệ thống sẽ vô hiệu hóa khung giờ đặt khám của ngày này)")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                                "ĐĂNG KÝ NGHỈ PHÉP", "Bác sĩ đăng ký nghỉ ngày " + selectedLeaveDate + " - Lý do: " + reason);
                        Toast.makeText(this, "Đã gửi đăng ký nghỉ phép thành công!", Toast.LENGTH_LONG).show();
                        etLeaveDate.setText("");
                        etLeaveReason.setText("");
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
