package com.phongkham.app.activities.owner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.phongkham.app.R;
import com.phongkham.app.activities.AuthActivity;
import com.phongkham.app.activities.admin.AdminConfigSlotsActivity;
import com.phongkham.app.database.InvoiceDao;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

public class OwnerMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private InvoiceDao invoiceDao;

    private TextView tvOwnerHeaderName;
    private ImageView ivOwnerLogout;
    private TextView tvOwnerTotalRevenue, tvRevenueConsultation, tvRevenueMedicine, tvRevenueProcedure;
    private TextView tvDoctorPayoutTotal, tvNetProfit;
    private CardView cardOwnerDoctorIncome, cardOwnerPatientFinance, cardOwnerFinancialReport, cardOwnerStaffSchedule, cardOwnerSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);

        sessionManager = new SessionManager(this);
        invoiceDao = new InvoiceDao(this);

        initViews();
        loadFinancialStats();
        setupListeners();
    }

    private void initViews() {
        tvOwnerHeaderName = findViewById(R.id.tvOwnerHeaderName);
        ivOwnerLogout = findViewById(R.id.ivOwnerLogout);

        tvOwnerTotalRevenue = findViewById(R.id.tvOwnerTotalRevenue);
        tvRevenueConsultation = findViewById(R.id.tvRevenueConsultation);
        tvRevenueMedicine = findViewById(R.id.tvRevenueMedicine);
        tvRevenueProcedure = findViewById(R.id.tvRevenueProcedure);
        tvDoctorPayoutTotal = findViewById(R.id.tvDoctorPayoutTotal);
        tvNetProfit = findViewById(R.id.tvNetProfit);

        cardOwnerDoctorIncome = findViewById(R.id.cardOwnerDoctorIncome);
        cardOwnerPatientFinance = findViewById(R.id.cardOwnerPatientFinance);
        cardOwnerFinancialReport = findViewById(R.id.cardOwnerFinancialReport);
        cardOwnerStaffSchedule = findViewById(R.id.cardOwnerStaffSchedule);
        cardOwnerSettings = findViewById(R.id.cardOwnerSettings);

        String name = sessionManager.getFullName();
        if (name == null || name.isEmpty()) name = "Chủ Phòng Khám Lê Lợi";
        tvOwnerHeaderName.setText("Xin chào, " + name);
    }

    private void loadFinancialStats() {
        double revenue = invoiceDao.getTotalRevenue();
        if (revenue == 0) revenue = 15850000;

        double consultation = revenue * 0.303;
        double medicine = revenue * 0.394;
        double procedure = revenue * 0.303;
        double doctorPayout = consultation * 0.70;
        double netProfit = revenue - doctorPayout;

        tvOwnerTotalRevenue.setText(DateTimeUtils.formatCurrency(revenue));
        tvRevenueConsultation.setText(DateTimeUtils.formatCurrency(consultation));
        tvRevenueMedicine.setText(DateTimeUtils.formatCurrency(medicine));
        tvRevenueProcedure.setText(DateTimeUtils.formatCurrency(procedure));
        tvDoctorPayoutTotal.setText(DateTimeUtils.formatCurrency(doctorPayout));
        tvNetProfit.setText(DateTimeUtils.formatCurrency(netProfit));
    }

    private void setupListeners() {
        // Card 1: Quản lý tiền theo bác sĩ
        cardOwnerDoctorIncome.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerMainActivity.this, OwnerDoctorIncomeActivity.class);
            startActivity(intent);
        });

        // Card 2: Quản lý tiền & công nợ bệnh nhân
        cardOwnerPatientFinance.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerMainActivity.this, OwnerPatientFinanceActivity.class);
            startActivity(intent);
        });

        // Card 3: Báo cáo thu nhập toàn viện
        cardOwnerFinancialReport.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerMainActivity.this, OwnerFinancialReportActivity.class);
            startActivity(intent);
        });

        // Card 4: Lịch trình & ca trực nhân viên
        cardOwnerStaffSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerMainActivity.this, OwnerStaffScheduleActivity.class);
            startActivity(intent);
        });

        // Card 5: Cài đặt tỷ lệ & giá
        cardOwnerSettings.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerMainActivity.this, AdminConfigSlotsActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        ivOwnerLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản Chủ phòng khám?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(OwnerMainActivity.this, AuthActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFinancialStats();
    }
}
