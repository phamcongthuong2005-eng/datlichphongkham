package com.phongkham.app.activities.patient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.UserDao;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

public class PatientProfileActivity extends AppCompatActivity {
    private ImageView ivBack;
    private EditText etProfileFullName, etProfilePhone, etProfileEmail, etProfileBirthDate, etProfileGender;
    private EditText etProfileInsurance, etProfileAddress, etProfileMedicalHistory, etProfileAllergies;
    private Button btnSaveProfile, btnNavChangePass, btnNavNotifications;

    private UserDao userDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadUserProfile();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etProfileFullName = findViewById(R.id.etProfileFullName);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfileBirthDate = findViewById(R.id.etProfileBirthDate);
        etProfileGender = findViewById(R.id.etProfileGender);
        etProfileInsurance = findViewById(R.id.etProfileInsurance);
        etProfileAddress = findViewById(R.id.etProfileAddress);
        etProfileMedicalHistory = findViewById(R.id.etProfileMedicalHistory);
        etProfileAllergies = findViewById(R.id.etProfileAllergies);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnNavChangePass = findViewById(R.id.btnNavChangePass);
        btnNavNotifications = findViewById(R.id.btnNavNotifications);
    }

    private void loadUserProfile() {
        int userId = sessionManager.getUserId();
        currentUser = userDao.getUserById(userId);

        if (currentUser != null) {
            etProfileFullName.setText(currentUser.getFullName());
            etProfilePhone.setText(currentUser.getPhone());
            etProfileEmail.setText(currentUser.getEmail());
            etProfileBirthDate.setText(currentUser.getBirthDate());
            etProfileGender.setText(currentUser.getGender());
            etProfileInsurance.setText(currentUser.getInsuranceNumber());
            etProfileAddress.setText(currentUser.getAddress());
            etProfileMedicalHistory.setText(currentUser.getMedicalHistory());
            etProfileAllergies.setText(currentUser.getAllergies());
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());
        btnNavChangePass.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, com.phongkham.app.activities.ChangePasswordActivity.class);
            startActivity(intent);
        });
        btnNavNotifications.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, com.phongkham.app.activities.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void handleSaveProfile() {
        if (currentUser == null) return;

        String fullName = etProfileFullName.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Họ tên và Số điện thoại không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setFullName(fullName);
        currentUser.setPhone(phone);
        currentUser.setEmail(etProfileEmail.getText().toString().trim());
        currentUser.setBirthDate(etProfileBirthDate.getText().toString().trim());
        currentUser.setGender(etProfileGender.getText().toString().trim());
        currentUser.setInsuranceNumber(etProfileInsurance.getText().toString().trim());
        currentUser.setAddress(etProfileAddress.getText().toString().trim());
        currentUser.setMedicalHistory(etProfileMedicalHistory.getText().toString().trim());
        currentUser.setAllergies(etProfileAllergies.getText().toString().trim());

        boolean ok = userDao.updateUserProfile(currentUser);
        if (ok) {
            auditLogDao.log(currentUser.getId(), currentUser.getFullName(), currentUser.getRole(),
                    "CẬP NHẬT HỒ SƠ", "Bệnh nhân đã cập nhật thông tin hồ sơ sức khỏe và BHYT");
            Toast.makeText(this, "Cập nhật hồ sơ sức khỏe thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi lưu hồ sơ!", Toast.LENGTH_SHORT).show();
        }
    }
}
