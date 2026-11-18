package com.phongkham.app.activities.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.phongkham.app.R;
import com.phongkham.app.activities.AuthActivity;
import com.phongkham.app.utils.SessionManager;

public class StaffMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    private TextView tvStaffHeaderName;
    private ImageView ivStaffLogout;
    private CardView cardStaffManageAppointments, cardStaffQueueToday, cardStaffWalkInBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);

        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvStaffHeaderName = findViewById(R.id.tvStaffHeaderName);
        ivStaffLogout = findViewById(R.id.ivStaffLogout);

        cardStaffManageAppointments = findViewById(R.id.cardStaffManageAppointments);
        cardStaffQueueToday = findViewById(R.id.cardStaffQueueToday);
        cardStaffWalkInBooking = findViewById(R.id.cardStaffWalkInBooking);

        String name = sessionManager.getFullName();
        if (name == null || name.isEmpty()) name = "Lê Thị Hương";
        tvStaffHeaderName.setText("Xin chào, " + name);
    }

    private void setupListeners() {
        // Card 1: Quản lý lịch hẹn phòng khám
        cardStaffManageAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(StaffMainActivity.this, StaffAppointmentsActivity.class);
            startActivity(intent);
        });

        // Card 2: Hàng chờ khám hôm nay
        cardStaffQueueToday.setOnClickListener(v -> {
            Intent intent = new Intent(StaffMainActivity.this, StaffQueueTodayActivity.class);
            startActivity(intent);
        });

        // Card 3: Đặt lịch hộ bệnh nhân vãng lai
        cardStaffWalkInBooking.setOnClickListener(v -> {
            Intent intent = new Intent(StaffMainActivity.this, StaffCreateBookingActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        ivStaffLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản Lễ tân?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(StaffMainActivity.this, AuthActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = sessionManager.getFullName();
        if (name == null || name.isEmpty()) name = "Lê Thị Hương";
        tvStaffHeaderName.setText("Xin chào, " + name);
    }
}
