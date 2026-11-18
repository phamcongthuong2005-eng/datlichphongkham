package com.phongkham.app.activities.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.phongkham.app.R;
import com.phongkham.app.activities.AuthActivity;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.database.UserDao;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.util.List;

public class DoctorMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DoctorDao doctorDao;
    private UserDao userDao;
    private AuditLogDao auditLogDao;

    private TextView tvDoctorHeaderName;
    private ImageView ivDoctorLogout;
    private TextView tvEditDoctorProfile;
    private TextView tvDoctorFullNameTitle, tvDoctorSpecialty, tvDoctorRoom, tvDoctorFee, tvDoctorExperience, tvDoctorPhone;
    private CardView cardDoctorAppointments, cardDoctorFlexibleSlots, cardDoctorWeeklySchedule;

    private Doctor currentDoctor;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        sessionManager = new SessionManager(this);
        doctorDao = new DoctorDao(this);
        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);

        initViews();
        loadDoctorProfile();
        setupListeners();
    }

    private void initViews() {
        tvDoctorHeaderName = findViewById(R.id.tvDoctorHeaderName);
        ivDoctorLogout = findViewById(R.id.ivDoctorLogout);
        tvEditDoctorProfile = findViewById(R.id.tvEditDoctorProfile);

        tvDoctorFullNameTitle = findViewById(R.id.tvDoctorFullNameTitle);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvDoctorRoom = findViewById(R.id.tvDoctorRoom);
        tvDoctorFee = findViewById(R.id.tvDoctorFee);
        tvDoctorExperience = findViewById(R.id.tvDoctorExperience);
        tvDoctorPhone = findViewById(R.id.tvDoctorPhone);

        cardDoctorAppointments = findViewById(R.id.cardDoctorAppointments);
        cardDoctorFlexibleSlots = findViewById(R.id.cardDoctorFlexibleSlots);
        cardDoctorWeeklySchedule = findViewById(R.id.cardDoctorWeeklySchedule);
    }

    private void loadDoctorProfile() {
        int userId = sessionManager.getUserId();
        currentUser = userDao.getUserById(userId);
        currentDoctor = doctorDao.getDoctorByUserId(userId);
        if (currentDoctor == null) {
            List<Doctor> all = doctorDao.getAllDoctors();
            if (!all.isEmpty()) currentDoctor = all.get(0);
        }

        if (currentDoctor != null) {
            String degree = currentDoctor.getDegree() != null ? currentDoctor.getDegree() + " " : "";
            tvDoctorHeaderName.setText("Xin chào, " + degree + currentDoctor.getFullName());
            tvDoctorFullNameTitle.setText(degree + currentDoctor.getFullName());
            tvDoctorSpecialty.setText("• Chuyên khoa: " + currentDoctor.getSpecialtyName());
            tvDoctorRoom.setText("• Phòng khám: Phòng 101");
            tvDoctorFee.setText("• Giá khám: " + DateTimeUtils.formatCurrency(currentDoctor.getConsultationFee()));
            tvDoctorExperience.setText("• Kinh nghiệm: " + currentDoctor.getExperienceYears() + " năm");

            String phone = (currentUser != null && currentUser.getPhone() != null && !currentUser.getPhone().isEmpty())
                    ? currentUser.getPhone() : "0900000002";
            tvDoctorPhone.setText("• SĐT: " + phone);
        }
    }

    private void setupListeners() {
        // Card 1: Danh sách ca khám
        cardDoctorAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorMainActivity.this, DoctorAppointmentsActivity.class);
            startActivity(intent);
        });

        // Card 2: Đóng / Mở Giờ Khám Linh Hoạt
        cardDoctorFlexibleSlots.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorMainActivity.this, DoctorSlotToggleActivity.class);
            startActivity(intent);
        });

        // Card 3: Mở Ca Khám & Lịch Tuần
        cardDoctorWeeklySchedule.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorMainActivity.this, DoctorScheduleActivity.class);
            intent.putExtra("doctor", currentDoctor);
            startActivity(intent);
        });

        // Chỉnh sửa hồ sơ bác sĩ
        tvEditDoctorProfile.setOnClickListener(v -> showEditProfileDialog());

        // Đăng xuất
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

    private void showEditProfileDialog() {
        if (currentDoctor == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa thông tin Bác sĩ");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        EditText etDoctorName = dialogView.findViewById(R.id.etDocName);
        EditText etDoctorDegree = dialogView.findViewById(R.id.etDocDegree);
        EditText etDoctorExp = dialogView.findViewById(R.id.etDocExp);
        EditText etDoctorFee = dialogView.findViewById(R.id.etDocFee);
        EditText etDoctorBio = dialogView.findViewById(R.id.etDocBio);

        etDoctorName.setText(currentDoctor.getFullName());
        etDoctorDegree.setText(currentDoctor.getDegree());
        etDoctorExp.setText(String.valueOf(currentDoctor.getExperienceYears()));
        etDoctorFee.setText(String.valueOf((long) currentDoctor.getConsultationFee()));
        etDoctorBio.setText(currentDoctor.getBio());

        builder.setView(dialogView);
        builder.setPositiveButton("Lưu cập nhật", (dialog, which) -> {
            currentDoctor.setFullName(etDoctorName.getText().toString().trim());
            currentDoctor.setDegree(etDoctorDegree.getText().toString().trim());
            try {
                currentDoctor.setExperienceYears(Integer.parseInt(etDoctorExp.getText().toString().trim()));
            } catch (Exception ignored) {}
            try {
                currentDoctor.setConsultationFee(Double.parseDouble(etDoctorFee.getText().toString().trim()));
            } catch (Exception ignored) {}
            currentDoctor.setBio(etDoctorBio.getText().toString().trim());

            doctorDao.updateDoctor(currentDoctor);
            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                    "CẬP NHẬT HỒ SƠ", "Bác sĩ đã cập nhật hồ sơ chuyên môn");
            Toast.makeText(this, "Đã cập nhật hồ sơ bác sĩ thành công!", Toast.LENGTH_SHORT).show();
            loadDoctorProfile();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctorProfile();
    }
}
