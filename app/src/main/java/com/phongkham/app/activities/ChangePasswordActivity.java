package com.phongkham.app.activities;

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

public class ChangePasswordActivity extends AppCompatActivity {
    private ImageView btnBack;
    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnSavePassword;

    private SessionManager sessionManager;
    private UserDao userDao;
    private AuditLogDao auditLogDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sessionManager = new SessionManager(this);
        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSavePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmNewPassword.getText().toString().trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có tối thiểu 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        User currentUser = userDao.getUserById(userId);

        if (currentUser == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentPass.equals(currentUser.getPassword())) {
            Toast.makeText(this, "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setPassword(newPass);
        boolean success = userDao.updateProfile(currentUser);
        if (success) {
            auditLogDao.log(userId, currentUser.getFullName(), currentUser.getRole(),
                    "ĐỔI MẬT KHẨU", "Người dùng tự đổi mật khẩu thành công");
            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Đổi mật khẩu thất bại. Vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
        }
    }
}
