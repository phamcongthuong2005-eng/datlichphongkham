package com.phongkham.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.activities.admin.AdminMainActivity;
import com.phongkham.app.activities.doctor.DoctorMainActivity;
import com.phongkham.app.activities.patient.PatientMainActivity;
import com.phongkham.app.activities.staff.StaffMainActivity;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.UserDao;
import com.phongkham.app.models.User;
import com.phongkham.app.network.TcpClient;
import com.phongkham.app.utils.SessionManager;

import org.json.JSONObject;

public class AuthActivity extends AppCompatActivity {
    private boolean isLoginMode = true;
    private UserDao userDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;

    private TextView tvAuthTitle, tvAuthSubtitle, tvForgotPassword, tvSwitchAuthMode;
    private LinearLayout layoutFullName;
    private EditText etFullName, etAccount, etPassword;
    private Button btnSubmitAuth;
    private Button btnQuickPatient, btnQuickDoctor, btnQuickStaff, btnQuickNurse, btnQuickAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSwitchAuthMode = findViewById(R.id.tvSwitchAuthMode);
        layoutFullName = findViewById(R.id.layoutFullName);
        etFullName = findViewById(R.id.etFullName);
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnSubmitAuth = findViewById(R.id.btnSubmitAuth);

        btnQuickPatient = findViewById(R.id.btnQuickPatient);
        btnQuickDoctor = findViewById(R.id.btnQuickDoctor);
        btnQuickStaff = findViewById(R.id.btnQuickStaff);
        btnQuickNurse = findViewById(R.id.btnQuickNurse);
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin);
    }

    private void setupListeners() {
        btnQuickPatient.setOnClickListener(v -> fillDemoAccount("benhnhan@gmail.com", "123456"));
        btnQuickDoctor.setOnClickListener(v -> fillDemoAccount("bacsi@gmail.com", "123456"));
        btnQuickStaff.setOnClickListener(v -> fillDemoAccount("letan@gmail.com", "123456"));
        if (btnQuickNurse != null) {
            btnQuickNurse.setOnClickListener(v -> fillDemoAccount("letan2@gmail.com", "123456"));
        }
        btnQuickAdmin.setOnClickListener(v -> fillDemoAccount("admin@gmail.com", "123456"));

        tvSwitchAuthMode.setOnClickListener(v -> toggleAuthMode());
        btnSubmitAuth.setOnClickListener(v -> handleAuthSubmit());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void fillDemoAccount(String account, String pass) {
        if (!isLoginMode) toggleAuthMode();
        etAccount.setText(account);
        etPassword.setText(pass);
    }

    private void toggleAuthMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvAuthTitle.setText("Phòng Khám Lê Lợi");
            tvAuthSubtitle.setText("Chuyên Khoa Cơ Xương Khớp & Cột Sống");
            layoutFullName.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            btnSubmitAuth.setText("ĐĂNG NHẬP");
            tvSwitchAuthMode.setText("Chưa có tài khoản? Đăng ký ngay");
        } else {
            tvAuthTitle.setText("Đăng Ký Khám Bệnh");
            tvAuthSubtitle.setText("Tạo hồ sơ bệnh nhân cơ xương khớp để đặt lịch");
            layoutFullName.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            btnSubmitAuth.setText("ĐĂNG KÝ");
            tvSwitchAuthMode.setText("Đã có tài khoản? Đăng nhập ngay");
        }
    }

    private void handleAuthSubmit() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoginMode) {
            JSONObject loginData = new JSONObject();
            try {
                loginData.put("account", account);
                loginData.put("password", password);
            } catch (Exception ignored) {}

            TcpClient.sendRequest("LOGIN", loginData, new TcpClient.ResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    JSONObject userObj = response.optJSONObject("user");
                    if (userObj != null) {
                        User user = new User();
                        user.setId(userObj.optInt("id"));
                        user.setFullName(userObj.optString("fullName"));
                        user.setEmail(userObj.optString("email"));
                        user.setPhone(userObj.optString("phone"));
                        user.setRole(userObj.optString("role"));
                        user.setLocked(userObj.optBoolean("isLocked", false));

                        sessionManager.createLoginSession(user);
                        Toast.makeText(AuthActivity.this, "Đăng nhập thành công! Xin chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
                        navigateToRoleHome(user.getRole());
                        return;
                    }
                    fallbackLocalLogin(account, password);
                }

                @Override
                public void onError(String errorMessage) {
                    fallbackLocalLogin(account, password);
                }
            });
        } else {
            String fullName = etFullName.getText().toString().trim();
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ và tên của bạn!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject regData = new JSONObject();
            try {
                regData.put("fullName", fullName);
                regData.put("phone", account);
                regData.put("email", account.contains("@") ? account : account + "@gmail.com");
                regData.put("password", password);
            } catch (Exception ignored) {}

            TcpClient.sendRequest("REGISTER", regData, new TcpClient.ResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    JSONObject userObj = response.optJSONObject("user");
                    if (userObj != null) {
                        User newUser = new User();
                        newUser.setId(userObj.optInt("id"));
                        newUser.setFullName(userObj.optString("fullName"));
                        newUser.setEmail(userObj.optString("email"));
                        newUser.setPhone(userObj.optString("phone"));
                        newUser.setRole(userObj.optString("role"));

                        sessionManager.createLoginSession(newUser);
                        Toast.makeText(AuthActivity.this, "Đăng ký thành công! Chào mừng " + newUser.getFullName(), Toast.LENGTH_SHORT).show();
                        navigateToRoleHome(User.ROLE_PATIENT);
                        return;
                    }
                    fallbackLocalRegister(fullName, account, password);
                }

                @Override
                public void onError(String errorMessage) {
                    fallbackLocalRegister(fullName, account, password);
                }
            });
        }
    }

    private void fallbackLocalLogin(String account, String password) {
        User user = userDao.login(account, password);
        if (user == null) {
            Toast.makeText(this, "Tài khoản hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user.isLocked()) {
            Toast.makeText(this, "Tài khoản này đã bị khóa bởi Quản trị viên!", Toast.LENGTH_LONG).show();
            return;
        }

        sessionManager.createLoginSession(user);
        auditLogDao.log(user.getId(), user.getFullName(), user.getRole(), "ĐĂNG NHẬP", "Đăng nhập thành công vào hệ thống");
        Toast.makeText(this, "Xin chào, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
        navigateToRoleHome(user.getRole());
    }

    private void fallbackLocalRegister(String fullName, String account, String password) {
        User newUser = new User();
        newUser.setFullName(fullName);
        if (account.contains("@")) {
            newUser.setEmail(account);
            newUser.setPhone("09" + ((int)(Math.random() * 89999999) + 10000000));
        } else {
            newUser.setPhone(account);
            newUser.setEmail(account + "@gmail.com");
        }
        newUser.setPassword(password);
        newUser.setRole(User.ROLE_PATIENT);

        boolean success = userDao.register(newUser);
        if (success) {
            sessionManager.createLoginSession(newUser);
            auditLogDao.log(newUser.getId(), newUser.getFullName(), newUser.getRole(), "ĐĂNG KÝ", "Đăng ký tài khoản bệnh nhân mới");
            Toast.makeText(this, "Đăng ký thành công! Đang chuyển đến màn hình chính...", Toast.LENGTH_SHORT).show();
            navigateToRoleHome(User.ROLE_PATIENT);
        } else {
            Toast.makeText(this, "Số điện thoại hoặc Email đã tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu / Đặt lại mật khẩu");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        EditText etResetAccount = view.findViewById(R.id.etResetAccount);
        EditText etNewPass = view.findViewById(R.id.etNewPass);
        builder.setView(view);

        builder.setPositiveButton("Cập nhật mật khẩu", (dialog, which) -> {
            String acc = etResetAccount.getText().toString().trim();
            String nPass = etNewPass.getText().toString().trim();
            if (acc.isEmpty() || nPass.isEmpty()) {
                Toast.makeText(AuthActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = userDao.resetPasswordByPhoneOrEmail(acc, nPass);
            if (ok) {
                Toast.makeText(AuthActivity.this, "Đặt lại mật khẩu thành công! Hãy đăng nhập lại.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AuthActivity.this, "Không tìm thấy tài khoản với Email/SĐT này!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void navigateToRoleHome(String role) {
        Intent intent;
        switch (role) {
            case User.ROLE_DOCTOR:
                intent = new Intent(this, DoctorMainActivity.class);
                break;
            case User.ROLE_STAFF:
                intent = new Intent(this, StaffMainActivity.class);
                break;
            case User.ROLE_NURSE:
                intent = new Intent(this, com.phongkham.app.activities.staff.NurseMainActivity.class);
                break;
            case User.ROLE_ADMIN:
                intent = new Intent(this, AdminMainActivity.class);
                break;
            default:
                intent = new Intent(this, PatientMainActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}
