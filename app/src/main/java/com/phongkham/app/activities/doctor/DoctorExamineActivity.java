package com.phongkham.app.activities.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.PrescriptionAdapter;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.MedicalRecordDao;
import com.phongkham.app.database.UserDao;
import com.phongkham.app.database.VitalSignDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.MedicalRecord;
import com.phongkham.app.models.PrescriptionItem;
import com.phongkham.app.models.User;
import com.phongkham.app.models.VitalSign;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DoctorExamineActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvExaminePatientName, tvExamineReason, tvExamineMedicalHistory, tvExamineVitals, tvVitalsAlert, tvAddMedicine;
    private EditText etExamineSymptoms, etDifferentialDiagnosis, etExamineDiagnosis, etExamineTests, etExamineNotes, etExamineFollowUpDate;
    private RecyclerView rvExaminePrescription;
    private Button btnCompleteExamine;

    private AppointmentDao appointmentDao;
    private MedicalRecordDao medicalRecordDao;
    private VitalSignDao vitalSignDao;
    private UserDao userDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;

    private Appointment appointment;
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();
    private PrescriptionAdapter prescriptionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_examine);

        appointmentDao = new AppointmentDao(this);
        medicalRecordDao = new MedicalRecordDao(this);
        vitalSignDao = new VitalSignDao(this);
        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        appointment = (Appointment) getIntent().getSerializableExtra("appointment");

        initViews();
        displayPatientData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvExaminePatientName = findViewById(R.id.tvExaminePatientName);
        tvExamineReason = findViewById(R.id.tvExamineReason);
        tvExamineMedicalHistory = findViewById(R.id.tvExamineMedicalHistory);
        tvExamineVitals = findViewById(R.id.tvExamineVitals);
        tvVitalsAlert = findViewById(R.id.tvVitalsAlert);
        tvAddMedicine = findViewById(R.id.tvAddMedicine);

        etExamineSymptoms = findViewById(R.id.etExamineSymptoms);
        etDifferentialDiagnosis = findViewById(R.id.etDifferentialDiagnosis);
        etExamineDiagnosis = findViewById(R.id.etExamineDiagnosis);
        etExamineTests = findViewById(R.id.etExamineTests);
        etExamineNotes = findViewById(R.id.etExamineNotes);
        etExamineFollowUpDate = findViewById(R.id.etExamineFollowUpDate);
        rvExaminePrescription = findViewById(R.id.rvExaminePrescription);
        btnCompleteExamine = findViewById(R.id.btnCompleteExamine);

        rvExaminePrescription.setLayoutManager(new LinearLayoutManager(this));
        prescriptionAdapter = new PrescriptionAdapter(this, prescriptionItems);
        rvExaminePrescription.setAdapter(prescriptionAdapter);
    }

    private void displayPatientData() {
        if (appointment == null) return;

        tvExaminePatientName.setText("Bệnh nhân: " + appointment.getPatientName() + " (Mã khám: " + appointment.getAppointmentCode() + ")");
        tvExamineReason.setText("Lý do khám: " + (appointment.getSymptoms() != null ? appointment.getSymptoms() : "Không có"));

        // Load medical history & allergies from patient profile
        User patientUser = userDao.getUserById(appointment.getPatientId());
        if (patientUser != null) {
            String history = "Tiền sử bệnh: " + (patientUser.getMedicalHistory() != null ? patientUser.getMedicalHistory() : "Không có") +
                    " • Dị ứng: " + (patientUser.getAllergies() != null ? patientUser.getAllergies() : "Không có");
            tvExamineMedicalHistory.setText(history);
        }

        // Load Vital Signs recorded by nurse (Menu 4.3)
        VitalSign vs = vitalSignDao.getVitalSignByAppointmentId(appointment.getId());
        if (vs != null) {
            double bmi = vs.calculateBmi();
            String vitalsStr = String.format("• Chiều cao: %.1f cm | Cân nặng: %.1f kg | BMI: %.1f\n• Huyết áp: %s mmHg | Nhịp tim: %d bpm\n• Thân nhiệt: %.1f°C | SpO2: %d%%\n• Ghi chú điều dưỡng: %s",
                    vs.getHeight(), vs.getWeight(), bmi, vs.getBloodPressure(), vs.getHeartRate(), vs.getTemperature(), vs.getSpo2(),
                    vs.getOtherNotes() != null && !vs.getOtherNotes().isEmpty() ? vs.getOtherNotes() : "Bình thường");
            tvExamineVitals.setText(vitalsStr);

            if (vs.getWarningAlert() != null && !vs.getWarningAlert().isEmpty()) {
                tvVitalsAlert.setVisibility(View.VISIBLE);
                tvVitalsAlert.setText("⚠️ " + vs.getWarningAlert());
            } else {
                tvVitalsAlert.setVisibility(View.GONE);
            }
        } else {
            tvExamineVitals.setText("• Chưa có số đo sinh hiệu lúc tiếp nhận (Đang dùng trị số tiêu chuẩn).");
            tvVitalsAlert.setVisibility(View.GONE);
        }

        // If existing record exists, load it
        MedicalRecord record = medicalRecordDao.getRecordByAppointmentId(appointment.getId());
        if (record != null) {
            etExamineSymptoms.setText(record.getSymptoms());
            etDifferentialDiagnosis.setText(record.getDifferentialDiagnosis());
            etExamineDiagnosis.setText(record.getDiagnosis());
            etExamineTests.setText(record.getTestsRequired());
            etExamineNotes.setText(record.getNotes());
            etExamineFollowUpDate.setText(record.getFollowUpDate());
        } else {
            etExamineSymptoms.setText(appointment.getSymptoms());
            String sym = appointment.getSymptoms() != null ? appointment.getSymptoms().toLowerCase() : "";
            if (sym.contains("lưng") || sym.contains("cột sống") || sym.contains("chân")) {
                etExamineDiagnosis.setText("Thoát vị đĩa đệm cột sống thắt lưng L4-L5 (M51.2)");
                etDifferentialDiagnosis.setText("Thoái hóa cột sống thắt lưng / Chèn ép rễ thần kinh tọa");
                etExamineTests.setText("Chụp X-Quang cột sống thắt lưng thẳng-nghiêng, Đo mật độ xương DEXA");
                etExamineNotes.setText("Tránh ngồi xổm hoặc mang vác nặng; tập bài tập vật lý trị liệu giãn cơ lưng");
                etExamineFollowUpDate.setText("Tái khám sau 14 ngày");
            } else if (sym.contains("gối") || sym.contains("khớp")) {
                etExamineDiagnosis.setText("Thoái hóa khớp gối nguyên phát độ II (M17.0)");
                etDifferentialDiagnosis.setText("Viêm bao hoạt dịch / Tràn dịch khớp gối nhẹ");
                etExamineTests.setText("Chụp X-Quang khớp gối hai bên, Siêu âm khớp gối");
                etExamineNotes.setText("Hạn chế đi cầu thang; hướng dẫn bài tập tăng cường cơ tứ đầu đùi");
                etExamineFollowUpDate.setText("Tái khám sau 1 tháng");
            } else {
                etExamineDiagnosis.setText("Hội chứng đau cơ xương khớp cấp (M79.1)");
            }

            // Thuốc chuyên khoa Cơ Xương Khớp mẫu
            prescriptionItems.add(new PrescriptionItem("Celecoxib 200mg", 14, "viên", "1 viên/lần x 2 lần/ngày", "Uống sau ăn no (kháng viêm giảm đau khớp)"));
            prescriptionItems.add(new PrescriptionItem("Eperisone HCl 50mg", 20, "viên", "1 viên/lần x 2 lần/ngày", "Uống sau ăn (thuốc giãn cơ vân)"));
            prescriptionItems.add(new PrescriptionItem("Glucosamine 500mg", 30, "viên", "1 viên/lần x 2 lần/ngày", "Uống sáng và chiều (nuôi dưỡng sụn khớp)"));
            prescriptionAdapter.notifyDataSetChanged();
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        tvAddMedicine.setOnClickListener(v -> showAddMedicineDialog());
        btnCompleteExamine.setOnClickListener(v -> handleCompleteExamine());
    }

    private void showAddMedicineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kê thêm thuốc điều trị");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medicine, null);
        EditText etMedName = view.findViewById(R.id.etMedName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etUnit = view.findViewById(R.id.etUnit);
        EditText etDosage = view.findViewById(R.id.etDosage);
        EditText etInstruction = view.findViewById(R.id.etInstruction);
        builder.setView(view);

        builder.setPositiveButton("Thêm vào đơn", (dialog, which) -> {
            String name = etMedName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String instruction = etInstruction.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(DoctorExamineActivity.this, "Vui lòng nhập tên thuốc!", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = 1;
            try {
                qty = Integer.parseInt(qtyStr);
            } catch (Exception ignored) {}

            prescriptionItems.add(new PrescriptionItem(name, qty, unit.isEmpty() ? "viên" : unit, dosage, instruction));
            prescriptionAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void handleCompleteExamine() {
        if (appointment == null) return;

        String diagnosis = etExamineDiagnosis.getText().toString().trim();
        if (diagnosis.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán xác định cho bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(appointment.getId());
        record.setPatientId(appointment.getPatientId());
        record.setDoctorId(appointment.getDoctorId());
        record.setSymptoms(etExamineSymptoms.getText().toString().trim());
        record.setDifferentialDiagnosis(etDifferentialDiagnosis.getText().toString().trim());
        record.setDiagnosis(diagnosis);
        record.setTestsRequired(etExamineTests.getText().toString().trim());
        record.setNotes(etExamineNotes.getText().toString().trim());
        record.setFollowUpDate(etExamineFollowUpDate.getText().toString().trim());
        record.setPrescriptionItems(prescriptionItems);

        boolean ok = medicalRecordDao.saveRecord(record);
        if (ok) {
            // Update appointment status to COMPLETED
            appointmentDao.updateStatus(appointment.getId(), Appointment.STATUS_COMPLETED);

            // Audit log: Thao tác sửa/ghi kết quả khám được ghi nhật ký (Menu 4.9 & 5.5)
            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                    "KẾT QUẢ KHÁM BỆNH", "Bác sĩ hoàn tất khám, chẩn đoán: '" + diagnosis + "' cho BN " + appointment.getPatientName() + " (Mã: " + appointment.getAppointmentCode() + ")");

            Toast.makeText(this, "Đã lưu hồ sơ khám bệnh & in đơn thuốc thành công!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu kết quả khám!", Toast.LENGTH_SHORT).show();
        }
    }
}
