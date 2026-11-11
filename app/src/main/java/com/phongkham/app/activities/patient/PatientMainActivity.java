package com.phongkham.app.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.activities.AuthActivity;
import com.phongkham.app.adapters.DoctorAdapter;
import com.phongkham.app.adapters.SpecialtyAdapter;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.Specialty;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PatientMainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DoctorDao doctorDao;

    private TextView tvWelcomePatient, tvResetFilter;
    private ImageView ivLogout;
    private EditText etSearchDoctor;
    private CardView cardBookAction, cardMyAppointments, cardMyProfile;
    private RecyclerView rvSpecialties, rvDoctors;

    private List<Doctor> doctorList = new ArrayList<>();
    private DoctorAdapter doctorAdapter;
    private Integer selectedSpecialtyId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        sessionManager = new SessionManager(this);
        doctorDao = new DoctorDao(this);

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        tvWelcomePatient = findViewById(R.id.tvWelcomePatient);
        tvResetFilter = findViewById(R.id.tvResetFilter);
        ivLogout = findViewById(R.id.ivLogout);
        etSearchDoctor = findViewById(R.id.etSearchDoctor);
        cardBookAction = findViewById(R.id.cardBookAction);
        cardMyAppointments = findViewById(R.id.cardMyAppointments);
        cardMyProfile = findViewById(R.id.cardMyProfile);
        rvSpecialties = findViewById(R.id.rvSpecialties);
        rvDoctors = findViewById(R.id.rvDoctors);

        tvWelcomePatient.setText("Xin chào, " + sessionManager.getFullName());
    }

    private void setupData() {
        // Specialties Horizontal List
        List<Specialty> specialties = doctorDao.getAllSpecialties();
        SpecialtyAdapter specialtyAdapter = new SpecialtyAdapter(this, specialties, specialty -> {
            selectedSpecialtyId = specialty.getId();
            filterDoctors();
            Toast.makeText(this, "Đang lọc dịch vụ: " + specialty.getName(), Toast.LENGTH_SHORT).show();
        });
        rvSpecialties.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSpecialties.setAdapter(specialtyAdapter);

        // Doctors Vertical List
        doctorList.clear();
        doctorList.addAll(doctorDao.getAllDoctors());
        doctorAdapter = new DoctorAdapter(this, doctorList, new DoctorAdapter.OnDoctorClickListener() {
            @Override
            public void onDoctorClick(Doctor doctor) {
                Intent intent = new Intent(PatientMainActivity.this, DoctorDetailActivity.class);
                intent.putExtra("doctor", doctor);
                startActivity(intent);
            }

            @Override
            public void onBookClick(Doctor doctor) {
                Intent intent = new Intent(PatientMainActivity.this, BookAppointmentActivity.class);
                intent.putExtra("selectedDoctorId", doctor.getId());
                startActivity(intent);
            }
        });
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(doctorAdapter);
    }

    private void setupListeners() {
        cardBookAction.setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)));
        cardMyAppointments.setOnClickListener(v -> startActivity(new Intent(this, PatientAppointmentsActivity.class)));
        cardMyProfile.setOnClickListener(v -> startActivity(new Intent(this, PatientProfileActivity.class)));

        tvResetFilter.setOnClickListener(v -> {
            selectedSpecialtyId = null;
            etSearchDoctor.setText("");
            filterDoctors();
        });

        etSearchDoctor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(PatientMainActivity.this, AuthActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void filterDoctors() {
        String keyword = etSearchDoctor.getText().toString().trim();
        List<Doctor> filtered = doctorDao.searchDoctors(keyword, selectedSpecialtyId);
        doctorList.clear();
        doctorList.addAll(filtered);
        doctorAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        filterDoctors();
    }
}
