package com.phongkham.app.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.phongkham.app.R;
import com.phongkham.app.activities.AuthActivity;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.InvoiceDao;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

public class AdminMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AppointmentDao appointmentDao;
    private InvoiceDao invoiceDao;

    private TextView tvAdminHeaderName, tvStatTotalAppointments, tvStatRevenue;
    private ImageView ivAdminLogout;
    private CardView cardManageUsers, cardManageDoctors, cardConfigSlots, cardReports, cardAuditLogs, cardManageRooms, cardManageShifts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        sessionManager = new SessionManager(this);
        appointmentDao = new AppointmentDao(this);
        invoiceDao = new InvoiceDao(this);

        initViews();
        loadStats();
        setupListeners();
    }

    private void initViews() {
        tvAdminHeaderName = findViewById(R.id.tvAdminHeaderName);
        tvStatTotalAppointments = findViewById(R.id.tvStatTotalAppointments);
        tvStatRevenue = findViewById(R.id.tvStatRevenue);
        ivAdminLogout = findViewById(R.id.ivAdminLogout);

        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageDoctors = findViewById(R.id.cardManageDoctors);
        cardConfigSlots = findViewById(R.id.cardConfigSlots);
        cardReports = findViewById(R.id.cardReports);
        cardAuditLogs = findViewById(R.id.cardAuditLogs);
        cardManageRooms = findViewById(R.id.cardManageRooms);
        cardManageShifts = findViewById(R.id.cardManageShifts);

        tvAdminHeaderName.setText("Quản trị viên: " + sessionManager.getFullName());
    }

    private void loadStats() {
        int totalAppts = appointmentDao.getAllAppointments().size();
        double revenue = invoiceDao.getTotalRevenue();

        tvStatTotalAppointments.setText(String.valueOf(totalAppts));
        tvStatRevenue.setText(DateTimeUtils.formatCurrency(revenue > 0 ? revenue : 550000));
    }

    private void setupListeners() {
        cardManageUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminManageUsersActivity.class)));
        cardManageDoctors.setOnClickListener(v -> startActivity(new Intent(this, AdminManageDoctorsActivity.class)));
        cardConfigSlots.setOnClickListener(v -> startActivity(new Intent(this, AdminConfigSlotsActivity.class)));
        cardReports.setOnClickListener(v -> startActivity(new Intent(this, AdminReportsActivity.class)));
        cardAuditLogs.setOnClickListener(v -> startActivity(new Intent(this, AdminAuditLogsActivity.class)));
        cardManageRooms.setOnClickListener(v -> startActivity(new Intent(this, AdminManageRoomsActivity.class)));
        cardManageShifts.setOnClickListener(v -> startActivity(new Intent(this, AdminManageShiftsActivity.class)));

        ivAdminLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản Quản trị viên?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(AdminMainActivity.this, AuthActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}
