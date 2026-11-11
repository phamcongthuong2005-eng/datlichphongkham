package com.phongkham.app.activities.patient;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.PrescriptionAdapter;
import com.phongkham.app.database.MedicalRecordDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.MedicalRecord;
import com.phongkham.app.models.PrescriptionItem;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDetailActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvRecordCode, tvRecordDoctor, tvRecordDate, tvSymptomsDetail, tvDiagnosisDetail, tvTestsDetail, tvDoctorNotes, tvFollowUpDate;
    private RecyclerView rvPrescription;

    private MedicalRecordDao medicalRecordDao;
    private Appointment appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record_detail);

        medicalRecordDao = new MedicalRecordDao(this);
        appointment = (Appointment) getIntent().getSerializableExtra("appointment");

        initViews();
        loadMedicalRecord();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvRecordCode = findViewById(R.id.tvRecordCode);
        tvRecordDoctor = findViewById(R.id.tvRecordDoctor);
        tvRecordDate = findViewById(R.id.tvRecordDate);
        tvSymptomsDetail = findViewById(R.id.tvSymptomsDetail);
        tvDiagnosisDetail = findViewById(R.id.tvDiagnosisDetail);
        tvTestsDetail = findViewById(R.id.tvTestsDetail);
        tvDoctorNotes = findViewById(R.id.tvDoctorNotes);
        tvFollowUpDate = findViewById(R.id.tvFollowUpDate);
        rvPrescription = findViewById(R.id.rvPrescription);

        ivBack.setOnClickListener(v -> finish());
        rvPrescription.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadMedicalRecord() {
        if (appointment == null) return;

        tvRecordCode.setText("Mã khám: " + appointment.getAppointmentCode());
        tvRecordDoctor.setText("Bác sĩ khám: " + appointment.getDoctorName() + " (" + appointment.getSpecialtyName() + ")");
        tvRecordDate.setText("Ngày khám: " + appointment.getAppointmentDate() + " (" + appointment.getTimeSlot() + ")");

        MedicalRecord record = medicalRecordDao.getRecordByAppointmentId(appointment.getId());
        List<PrescriptionItem> prescriptionItems = new ArrayList<>();

        if (record != null) {
            tvSymptomsDetail.setText("Triệu chứng: " + (record.getSymptoms() != null ? record.getSymptoms() : appointment.getSymptoms()));
            tvDiagnosisDetail.setText("Chẩn đoán: " + (record.getDiagnosis() != null ? record.getDiagnosis() : "Chưa có chẩn đoán"));
            tvTestsDetail.setText("Chỉ định xét nghiệm: " + (record.getTestsRequired() != null && !record.getTestsRequired().isEmpty() ? record.getTestsRequired() : "Không có"));
            tvDoctorNotes.setText("Lời dặn bác sĩ: " + (record.getNotes() != null ? record.getNotes() : "Tuân thủ hướng dẫn dùng thuốc và nghỉ ngơi hợp lý."));
            tvFollowUpDate.setText("Tái khám: " + (record.getFollowUpDate() != null && !record.getFollowUpDate().isEmpty() ? record.getFollowUpDate() : "Khi có triệu chứng bất thường"));

            // Parse or mockup prescription items
            prescriptionItems.add(new PrescriptionItem("Omeprazol 20mg", 14, "viên", "1 viên x 1 lần/ngày", "Uống trước ăn sáng 30 phút"));
            prescriptionItems.add(new PrescriptionItem("Phosphalugel (Gel chữ P)", 20, "gói", "1 gói x 2 lần/ngày", "Uống khi có cơn đau thượng vị"));
        } else {
            tvSymptomsDetail.setText("Triệu chứng: " + appointment.getSymptoms());
            tvDiagnosisDetail.setText("Chẩn đoán: Viêm dạ dày - tá tràng");
            tvTestsDetail.setText("Chỉ định: Nội soi dạ dày");
            tvDoctorNotes.setText("Lời dặn: Ăn chín uống sôi, không ăn cay.");
            tvFollowUpDate.setText("Tái khám: Sau 1 tuần");

            prescriptionItems.add(new PrescriptionItem("Paracetamol 500mg", 10, "viên", "1 viên khi đau/sốt", "Sau ăn"));
            prescriptionItems.add(new PrescriptionItem("Vitamin C 500mg", 10, "viên", "1 viên/ngày", "Sáng sau ăn"));
        }

        PrescriptionAdapter adapter = new PrescriptionAdapter(this, prescriptionItems);
        rvPrescription.setAdapter(adapter);
    }
}
