package com.phongkham.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.activities.admin.AdminMainActivity;
import com.phongkham.app.activities.doctor.DoctorMainActivity;
import com.phongkham.app.activities.patient.PatientMainActivity;
import com.phongkham.app.activities.staff.StaffMainActivity;
import com.phongkham.app.database.DatabaseHelper;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pre-initialize SQLite Database
        DatabaseHelper.getInstance(this).getWritableDatabase();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            if (session.isLoggedIn()) {
                navigateToRoleHome(session.getRole());
            } else {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                finish();
            }
        }, 1200);
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
