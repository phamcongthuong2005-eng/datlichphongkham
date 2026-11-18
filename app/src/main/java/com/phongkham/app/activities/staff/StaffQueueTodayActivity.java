package com.phongkham.app.activities.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.AppointmentAdapter;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class StaffQueueTodayActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AppointmentDao appointmentDao;
    private DoctorDao doctorDao;
    private AuditLogDao auditLogDao;

    private ImageView ivBack;
    private Spinner spinnerQueueDoctor;
    private TextView tvQueueCount, tvQueueEmptyState;
    private RecyclerView rvStaffQueue;

    private List<Doctor> doctorList = new ArrayList<>();
    private List<Appointment> queueList = new ArrayList<>();
    private AppointmentAdapter adapter;
    private Integer selectedDoctorId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_queue_today);

        sessionManager = new SessionManager(this);
        appointmentDao = new AppointmentDao(this);
        doctorDao = new DoctorDao(this);
        auditLogDao = new AuditLogDao(this);

        initViews();
        setupDoctorSpinner();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        spinnerQueueDoctor = findViewById(R.id.spinnerQueueDoctor);
        tvQueueCount = findViewById(R.id.tvQueueCount);
        tvQueueEmptyState = findViewById(R.id.tvQueueEmptyState);
        rvStaffQueue = findViewById(R.id.rvStaffQueue);

        ivBack.setOnClickListener(v -> finish());
        rvStaffQueue.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDoctorSpinner() {
        doctorList = doctorDao.getAllDoctors();
        List<String> names = new ArrayList<>();
        names.add("--- Tất cả bác sĩ hôm nay ---");
        for (Doctor d : doctorList) {
            names.add(d.getFullName() + " (" + d.getSpecialtyName() + ")");
        }

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerQueueDoctor.setAdapter(spinAdapter);

        spinnerQueueDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDoctorId = null;
                } else {
                    selectedDoctorId = doctorList.get(position - 1).getId();
                }
                loadQueue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadQueue() {
        String today = DateTimeUtils.getToday();
        List<Appointment> allToday = appointmentDao.getAppointmentsByDate(today);

        queueList.clear();
        for (Appointment a : allToday) {
            // Chỉ lấy các bệnh nhân đã check-in hoặc đang trong quy trình khám
            boolean isCheckedIn = a.getQueueNumber() > 0 ||
                    Appointment.STATUS_CONFIRMED.equals(a.getStatus()) ||
                    Appointment.STATUS_WAITING.equals(a.getStatus()) ||
                    Appointment.STATUS_CALLED.equals(a.getStatus()) ||
                    Appointment.STATUS_EXAMINING.equals(a.getStatus());

            if (isCheckedIn) {
                if (selectedDoctorId == null || a.getDoctorId() == selectedDoctorId) {
                    queueList.add(a);
                }
            }
        }

        tvQueueCount.setText("Hàng chờ hôm nay: " + queueList.size() + " bệnh nhân");
        if (queueList.isEmpty()) {
            rvStaffQueue.setVisibility(View.GONE);
            tvQueueEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvStaffQueue.setVisibility(View.VISIBLE);
            tvQueueEmptyState.setVisibility(View.GONE);

            adapter = new AppointmentAdapter(this, queueList, User.ROLE_STAFF, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    Toast.makeText(StaffQueueTodayActivity.this, "Đã gọi loa mời BN " + appointment.getPatientName() + " (STT " + appointment.getQueueNumber() + ") vào khám!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {}

                @Override
                public void onItemClick(Appointment appointment) {
                    Toast.makeText(StaffQueueTodayActivity.this, "Bệnh nhân: " + appointment.getPatientName() + " - STT " + appointment.getQueueNumber(), Toast.LENGTH_SHORT).show();
                }
            });
            rvStaffQueue.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQueue();
    }
}
