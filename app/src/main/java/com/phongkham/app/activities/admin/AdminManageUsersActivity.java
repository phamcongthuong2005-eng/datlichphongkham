package com.phongkham.app.activities.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.UserAdapter;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.UserDao;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvUserCount;
    private RecyclerView rvAdminUsers;

    private UserDao userDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private List<User> userList;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadUsers();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvUserCount = findViewById(R.id.tvUserCount);
        rvAdminUsers = findViewById(R.id.rvAdminUsers);

        ivBack.setOnClickListener(v -> finish());
        rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadUsers() {
        userList = userDao.getAllUsers();
        tvUserCount.setText("Danh sách tài khoản hệ thống (" + userList.size() + " users):");

        adapter = new UserAdapter(this, userList, user -> {
            boolean willLock = !user.isLocked();
            new AlertDialog.Builder(this)
                    .setTitle(willLock ? "Khóa tài khoản" : "Mở khóa tài khoản")
                    .setMessage("Bạn có chắc chắn muốn " + (willLock ? "KHÓA" : "MỞ KHÓA") + " tài khoản của: " + user.getFullName() + " (" + user.getRole() + ")?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        boolean ok = userDao.toggleLockUser(user.getId(), willLock);
                        if (ok) {
                            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                                    "QUẢN LÝ USER", (willLock ? "Khóa" : "Mở khóa") + " tài khoản " + user.getFullName() + " (" + user.getEmail() + ")");
                            Toast.makeText(this, (willLock ? "Đã khóa" : "Đã mở khóa") + " tài khoản thành công!", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        rvAdminUsers.setAdapter(adapter);
    }
}
