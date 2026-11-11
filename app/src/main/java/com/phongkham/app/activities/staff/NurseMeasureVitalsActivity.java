package com.phongkham.app.activities.staff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.NotificationDao;
import com.phongkham.app.database.VitalSignDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.User;
import com.phongkham.app.models.VitalSign;
import com.phongkham.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NurseMeasureVitalsActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvVitalsPatientInfo, tvVitalsDoctorInfo, tvAbnormalWarning;
    private EditText etHeight, etWeight, etTemperature, etBloodPressure, etHeartRate, etSpo2, etVitalsNotes;
    private Button btnSaveAndTransfer;

    private VitalSignDao vitalSignDao;
    private AppointmentDao appointmentDao;
    private NotificationDao notificationDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private Appointment appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_measure_vitals);

        vitalSignDao = new VitalSignDao(this);
        appointmentDao = new AppointmentDao(this);
        notificationDao = new NotificationDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        appointment = (Appointment) getIntent().getSerializableExtra("appointment");

        initViews();
        displayData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvVitalsPatientInfo = findViewById(R.id.tvVitalsPatientInfo);
        tvVitalsDoctorInfo = findViewById(R.id.tvVitalsDoctorInfo);
        tvAbnormalWarning = findViewById(R.id.tvAbnormalWarning);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etTemperature = findViewById(R.id.etTemperature);
        etBloodPressure = findViewById(R.id.etBloodPressure);
        etHeartRate = findViewById(R.id.etHeartRate);
        etSpo2 = findViewById(R.id.etSpo2);
        etVitalsNotes = findViewById(R.id.etVitalsNotes);
        btnSaveAndTransfer = findViewById(R.id.btnSaveAndTransfer);
    }

    private void displayData() {
        if (appointment == null) return;
        tvVitalsPatientInfo.setText("Bệnh nhân: " + appointment.getPatientName() + " (STT: #" + appointment.getQueueNumber() + ")");
        tvVitalsDoctorInfo.setText("Bác sĩ tiếp nhận: " + appointment.getDoctorName() + " • Phòng: " + appointment.getRoomCode());

        // Load existing vitals if any
        VitalSign existing = vitalSignDao.getVitalSignByAppointmentId(appointment.getId());
        if (existing != null) {
            etHeight.setText(String.valueOf(existing.getHeight()));
            etWeight.setText(String.valueOf(existing.getWeight()));
            etTemperature.setText(String.valueOf(existing.getTemperature()));
            etBloodPressure.setText(existing.getBloodPressure());
            etHeartRate.setText(String.valueOf(existing.getHeartRate()));
            etSpo2.setText(String.valueOf(existing.getSpo2()));
            etVitalsNotes.setText(existing.getOtherNotes());
            checkWarningAlerts();
        } else {
            etHeight.setText("170");
            etWeight.setText("65");
            etTemperature.setText("37.0");
            etBloodPressure.setText("120/80");
            etHeartRate.setText("75");
            etSpo2.setText("98");
        }
    }

    private void checkWarningAlerts() {
        double temp = parseDouble(etTemperature.getText().toString());
        int heart = parseInt(etHeartRate.getText().toString());
        int spo2 = parseInt(etSpo2.getText().toString());

        StringBuilder sb = new StringBuilder();
        if (temp >= 38.0) sb.append("• Sốt cao (≥ 38.0°C)\n");
        if (spo2 > 0 && spo2 < 95) sb.append("• SpO₂ thấp (< 95%)\n");
        if (heart > 100) sb.append("• Nhịp tim nhanh (> 100 bpm)\n");
        else if (heart > 0 && heart < 60) sb.append("• Nhịp tim chậm (< 60 bpm)\n");

        if (sb.length() > 0) {
            tvAbnormalWarning.setVisibility(View.VISIBLE);
            tvAbnormalWarning.setText("⚠️ CẢNH BÁO CHỈ SỐ BẤT THƯỜNG:\n" + sb.toString().trim());
        } else {
            tvAbnormalWarning.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkWarningAlerts();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTemperature.addTextChangedListener(watcher);
        etHeartRate.addTextChangedListener(watcher);
        etSpo2.addTextChangedListener(watcher);

        btnSaveAndTransfer.setOnClickListener(v -> handleSaveAndTransfer());
    }

    private void handleSaveAndTransfer() {
        if (appointment == null) return;

        double height = parseDouble(etHeight.getText().toString());
        double weight = parseDouble(etWeight.getText().toString());
        double temp = parseDouble(etTemperature.getText().toString());
        String bp = etBloodPressure.getText().toString().trim();
        int heart = parseInt(etHeartRate.getText().toString());
        int spo2 = parseInt(etSpo2.getText().toString());
        String notes = etVitalsNotes.getText().toString().trim();

        if (height <= 0 || weight <= 0 || temp <= 0 || bp.isEmpty() || heart <= 0 || spo2 <= 0) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các chỉ số sinh hiệu bắt buộc!", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        VitalSign vs = new VitalSign(appointment.getId(), appointment.getPatientId(), height, weight, temp, bp, heart, spo2, sessionManager.getFullName(), now);
        vs.setOtherNotes(notes);

        boolean ok = vitalSignDao.saveVitalSign(vs);
        if (ok) {
            // Update appointment status to WAITING for Doctor
            appointmentDao.updateStatus(appointment.getId(), Appointment.STATUS_WAITING);

            // Notify Doctor (Menu 3.9.5)
            notificationDao.addNotification(appointment.getDoctorId(), "Bệnh nhân đã sẵn sàng khám",
                    "Y tá " + sessionManager.getFullName() + " đã đo sinh hiệu và chuyển bệnh nhân " + appointment.getPatientName() + " (STT #" + appointment.getQueueNumber() + ") vào phòng " + appointment.getRoomCode(), "QUEUE");

            // Audit log (Menu 3.9.6)
            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_NURSE,
                    "ĐO SINH HIỆU & CHUYỂN BÁC SĨ", "Đo sinh hiệu và chuyển bệnh nhân " + appointment.getPatientName() + " cho " + appointment.getDoctorName() + " (Phòng " + appointment.getRoomCode() + ")");

            new AlertDialog.Builder(this)
                    .setTitle("✅ Hoàn Tất Đo Chỉ Số Sinh Hiệu")
                    .setMessage("Đã lưu chỉ số sinh hiệu thành công và chuyển bệnh nhân " + appointment.getPatientName() + " sang phòng khám " + appointment.getRoomCode() + " cho " + appointment.getDoctorName() + " tiếp nhận khám!")
                    .setPositiveButton("Xác nhận", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        } else {
            Toast.makeText(this, "Có lỗi khi lưu chỉ số sinh hiệu!", Toast.LENGTH_SHORT).show();
        }
    }

    private double parseDouble(String str) {
        try { return Double.parseDouble(str.trim()); } catch (Exception e) { return 0; }
    }

    private int parseInt(String str) {
        try { return Integer.parseInt(str.trim()); } catch (Exception e) { return 0; }
    }
}
