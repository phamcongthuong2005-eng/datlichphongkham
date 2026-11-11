package com.phongkham.app.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.utils.DateTimeUtils;

public class DoctorDetailActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvDoctorName, tvDoctorSpecialty, tvRating, tvExperience, tvFee, tvBio, tvWorkingDays;
    private Button btnBookThisDoctor;
    private Doctor doctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        doctor = (Doctor) getIntent().getSerializableExtra("doctor");

        initViews();
        displayData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvRating = findViewById(R.id.tvRating);
        tvExperience = findViewById(R.id.tvExperience);
        tvFee = findViewById(R.id.tvFee);
        tvBio = findViewById(R.id.tvBio);
        tvWorkingDays = findViewById(R.id.tvWorkingDays);
        btnBookThisDoctor = findViewById(R.id.btnBookThisDoctor);
    }

    private void displayData() {
        if (doctor != null) {
            tvDoctorName.setText(doctor.getFullName());
            tvDoctorSpecialty.setText("Chuyên khoa: " + doctor.getSpecialtyName() + " (" + doctor.getDegree() + ")");
            tvRating.setText("⭐ " + String.format("%.1f", doctor.getRating()));
            tvExperience.setText(doctor.getExperienceYears() + " năm");
            tvFee.setText(DateTimeUtils.formatCurrency(doctor.getConsultationFee()));
            tvBio.setText(doctor.getBio());
            tvWorkingDays.setText("📅 " + doctor.getWorkingDays() + " (Ca Sáng: 08:00 - 12:00 | Ca Tối: 15:00 - 20:00)");
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnBookThisDoctor.setOnClickListener(v -> {
            if (doctor != null) {
                Intent intent = new Intent(DoctorDetailActivity.this, BookAppointmentActivity.class);
                intent.putExtra("selectedDoctorId", doctor.getId());
                startActivity(intent);
                finish();
            }
        });
    }
}
