package com.phongkham.app.activities.patient;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.TimeSlotAdapter;
import com.phongkham.app.business.BusinessRulesValidator;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.Specialty;
import com.phongkham.app.models.TimeSlot;
import com.phongkham.app.models.User;
import com.phongkham.app.network.TcpClient;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {
    private DoctorDao doctorDao;
    private AppointmentDao appointmentDao;
    private com.phongkham.app.database.MedicalRecordDao medicalRecordDao;
    private AuditLogDao auditLogDao;
    private BusinessRulesValidator rulesValidator;
    private SessionManager sessionManager;

    private ImageView ivBack;
    private Spinner spinnerSpecialty, spinnerDoctor;
    private TextView tvDoctorSummary, tvDoctorWorkingDays;
    private EditText etSelectDate, etSymptoms;
    private RecyclerView rvTimeSlots;
    private Button btnConfirmBooking;

    private android.widget.RadioGroup rgAppointmentType;
    private android.widget.RadioButton rbFirstVisit, rbFollowUp;
    private android.widget.LinearLayout layoutFollowUpDetails;
    private Spinner spinnerOldRecords;
    private TextView tvOldRecordSummary;
    private List<com.phongkham.app.models.MedicalRecord> oldRecordsList = new ArrayList<>();
    private com.phongkham.app.models.MedicalRecord selectedOldRecord = null;

    private List<Specialty> specialtyList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private List<TimeSlot> slotList = new ArrayList<>();
    private TimeSlotAdapter slotAdapter;

    private Doctor currentSelectedDoctor = null;
    private String selectedDate = "";
    private int preSelectedDoctorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        doctorDao = new DoctorDao(this);
        appointmentDao = new AppointmentDao(this);
        medicalRecordDao = new com.phongkham.app.database.MedicalRecordDao(this);
        auditLogDao = new AuditLogDao(this);
        rulesValidator = new BusinessRulesValidator(this);
        sessionManager = new SessionManager(this);

        preSelectedDoctorId = getIntent().getIntExtra("selectedDoctorId", -1);

        initViews();
        setupSpecialties();
        setupDatePicker();
        setupFollowUpModule();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        tvDoctorSummary = findViewById(R.id.tvDoctorSummary);
        tvDoctorWorkingDays = findViewById(R.id.tvDoctorWorkingDays);
        etSelectDate = findViewById(R.id.etSelectDate);
        etSymptoms = findViewById(R.id.etSymptoms);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        rgAppointmentType = findViewById(R.id.rgAppointmentType);
        rbFirstVisit = findViewById(R.id.rbFirstVisit);
        rbFollowUp = findViewById(R.id.rbFollowUp);
        layoutFollowUpDetails = findViewById(R.id.layoutFollowUpDetails);
        spinnerOldRecords = findViewById(R.id.spinnerOldRecords);
        tvOldRecordSummary = findViewById(R.id.tvOldRecordSummary);

        selectedDate = DateTimeUtils.getToday();
        etSelectDate.setText(selectedDate);

        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupSpecialties() {
        specialtyList = doctorDao.getAllSpecialties();
        List<String> specialtyNames = new ArrayList<>();
        specialtyNames.add("--- Tất cả dịch vụ Cơ Xương Khớp ---");
        for (Specialty s : specialtyList) {
            specialtyNames.add(s.getName());
        }

        ArrayAdapter<String> specAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specialtyNames);
        spinnerSpecialty.setAdapter(specAdapter);

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadDoctors(null);
                } else {
                    Specialty selectedSpec = specialtyList.get(position - 1);
                    loadDoctors(selectedSpec.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDoctors(Integer specialtyId) {
        doctorList = doctorDao.searchDoctors("", specialtyId);
        List<String> doctorNames = new ArrayList<>();
        int selectIdx = 0;

        for (int i = 0; i < doctorList.size(); i++) {
            Doctor doc = doctorList.get(i);
            doctorNames.add(doc.getFullName() + " (" + doc.getSpecialtyName() + ")");
            if (doc.getId() == preSelectedDoctorId) {
                selectIdx = i;
            }
        }

        if (doctorNames.isEmpty()) {
            doctorNames.add("Không tìm thấy bác sĩ");
        }

        ArrayAdapter<String> docAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctorNames);
        spinnerDoctor.setAdapter(docAdapter);
        if (!doctorList.isEmpty()) {
            spinnerDoctor.setSelection(selectIdx);
        }

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < doctorList.size()) {
                    currentSelectedDoctor = doctorList.get(position);
                    updateDoctorInfoCard();
                    loadTimeSlots();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDoctorInfoCard() {
        if (currentSelectedDoctor != null) {
            tvDoctorSummary.setText("Bác sĩ: " + currentSelectedDoctor.getFullName() + " • Phí khám: " + DateTimeUtils.formatCurrency(currentSelectedDoctor.getConsultationFee()));
            tvDoctorWorkingDays.setText("Lịch trực: " + currentSelectedDoctor.getWorkingDays() + " • " + currentSelectedDoctor.getBio());
        }
    }

    private void setupFollowUpModule() {
        rgAppointmentType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFollowUp) {
                layoutFollowUpDetails.setVisibility(View.VISIBLE);
                loadPatientOldRecords();
            } else {
                layoutFollowUpDetails.setVisibility(View.GONE);
                selectedOldRecord = null;
                tvOldRecordSummary.setVisibility(View.GONE);
            }
        });
    }

    private void loadPatientOldRecords() {
        int patientId = sessionManager.getUserId();
        oldRecordsList = medicalRecordDao.getRecordsByPatientId(patientId);

        List<String> recordLabels = new ArrayList<>();
        if (oldRecordsList.isEmpty()) {
            recordLabels.add("--- Không tìm thấy hồ sơ cũ (Nhập mới bên dưới) ---");
            tvOldRecordSummary.setVisibility(View.VISIBLE);
            tvOldRecordSummary.setText("ℹ️ Chưa có lịch sử hồ sơ bệnh án cũ trong hệ thống. Bạn vẫn có thể điền triệu chứng và ghi chú tái khám ở mục bên dưới.");
        } else {
            recordLabels.add("--- Chọn bệnh án cũ để đối chiếu & cập nhật ---");
            for (com.phongkham.app.models.MedicalRecord mr : oldRecordsList) {
                recordLabels.add("Hồ sơ #" + mr.getId() + " - Chẩn đoán: " + mr.getDiagnosis() + " (" + mr.getCreatedAt() + ")");
            }
        }

        ArrayAdapter<String> recordAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, recordLabels);
        spinnerOldRecords.setAdapter(recordAdapter);

        spinnerOldRecords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position - 1 < oldRecordsList.size()) {
                    selectedOldRecord = oldRecordsList.get(position - 1);
                    tvOldRecordSummary.setVisibility(View.VISIBLE);
                    tvOldRecordSummary.setText("📌 Bệnh án cũ #" + selectedOldRecord.getId() + "\n" +
                            "• Chẩn đoán: " + selectedOldRecord.getDiagnosis() + "\n" +
                            "• Triệu chứng trước: " + selectedOldRecord.getSymptoms() + "\n" +
                            "• Lời dặn/Ghi chú: " + (selectedOldRecord.getNotes() != null ? selectedOldRecord.getNotes() : "Đúng hẹn tái khám"));

                    String currentSymptoms = etSymptoms.getText().toString().trim();
                    if (currentSymptoms.isEmpty()) {
                        etSymptoms.setText("[Tái khám theo hồ sơ #" + selectedOldRecord.getId() + " - " + selectedOldRecord.getDiagnosis() + "]: Tình trạng hiện tại: ");
                        etSymptoms.setSelection(etSymptoms.getText().length());
                    }
                } else {
                    selectedOldRecord = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDatePicker() {
        etSelectDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(y, m, d);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(selectedCal.getTime());
                etSelectDate.setText(selectedDate);
                loadTimeSlots();
            }, year, month, day);

            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
    }

    private void loadTimeSlots() {
        if (currentSelectedDoctor == null) return;

        slotList.clear();

        // 1. Kiểm tra bác sĩ có đăng ký nghỉ phép ngày này không
        if (doctorDao.isDoctorOnLeave(currentSelectedDoctor.getId(), selectedDate)) {
            Toast.makeText(this, "⚠️ Bác sĩ " + currentSelectedDoctor.getFullName() + " đã đăng ký nghỉ phép ngày " + selectedDate + "!\nVui lòng chọn ngày khám khác.", Toast.LENGTH_LONG).show();
            slotAdapter = new TimeSlotAdapter(this, slotList, slot -> {});
            rvTimeSlots.setAdapter(slotAdapter);
            return;
        }

        slotList = doctorDao.getTimeSlots(currentSelectedDoctor.getId(), selectedDate);
        if (slotList.isEmpty()) {
            String[] standardSlots = {
                    "08:00 - 08:30", "08:30 - 09:00", "09:00 - 09:30", "09:30 - 10:00",
                    "10:00 - 10:30", "10:30 - 11:00", "11:00 - 11:30", "11:30 - 12:00",
                    "15:00 - 15:30", "15:30 - 16:00", "16:00 - 16:30", "16:30 - 17:00",
                    "17:00 - 17:30", "17:30 - 18:00", "18:00 - 18:30", "18:30 - 19:00",
                    "19:00 - 19:30", "19:30 - 20:00"
            };
            for (String slotStr : standardSlots) {
                slotList.add(new TimeSlot(0, currentSelectedDoctor.getId(), selectedDate, slotStr, 3, 0));
            }
        }

        // Lọc các ca khám khả dụng
        List<TimeSlot> availableSlots = new ArrayList<>();
        for (TimeSlot s : slotList) {
            if (s.isAvailable()) {
                availableSlots.add(s);
            }
        }
        slotList = availableSlots;

        slotAdapter = new TimeSlotAdapter(this, slotList, slot -> {});
        rvTimeSlots.setAdapter(slotAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnConfirmBooking.setOnClickListener(v -> handleConfirmBooking());
    }

    private void handleConfirmBooking() {
        if (currentSelectedDoctor == null) {
            Toast.makeText(this, "Vui lòng chọn bác sĩ khám!", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlot selectedSlot = slotAdapter != null ? slotAdapter.getSelectedSlot() : null;
        if (selectedSlot == null) {
            Toast.makeText(this, "Vui lòng chọn 1 khung giờ khám còn trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        String symptoms = etSymptoms.getText().toString().trim();
        if (symptoms.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập triệu chứng hoặc lý do khám!", Toast.LENGTH_SHORT).show();
            return;
        }

        int patientId = sessionManager.getUserId();

        if (doctorDao.isDoctorOnLeave(currentSelectedDoctor.getId(), selectedDate)) {
            Toast.makeText(this, "⚠️ Bác sĩ đã đăng ký nghỉ phép ngày " + selectedDate + ". Không thể đặt lịch!", Toast.LENGTH_LONG).show();
            return;
        }

        if (rulesValidator.isPastDateTime(selectedDate, selectedSlot.getSlotTime())) {
            Toast.makeText(this, "Không thể đặt lịch cho khung giờ đã trôi qua trong quá khứ!", Toast.LENGTH_LONG).show();
            return;
        }

        if (rulesValidator.isSlotFull(currentSelectedDoctor.getId(), selectedDate, selectedSlot.getSlotTime())) {
            Toast.makeText(this, "Khung giờ này vừa mới đầy bệnh nhân. Vui lòng chọn khung giờ khác!", Toast.LENGTH_LONG).show();
            loadTimeSlots();
            return;
        }

        if (rulesValidator.hasPatientOverlap(patientId, selectedDate, selectedSlot.getSlotTime(), null)) {
            Toast.makeText(this, "Bạn đã có một lịch khám khác vào khung giờ này! Không thể đặt trùng.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isFollowUp = rbFollowUp != null && rbFollowUp.isChecked();
        String apptType = isFollowUp ? "Tái khám" : "Khám lần đầu";

        JSONObject bookData = new JSONObject();
        try {
            bookData.put("patientId", patientId);
            bookData.put("patientName", sessionManager.getFullName());
            bookData.put("doctorId", currentSelectedDoctor.getId());
            bookData.put("doctorName", currentSelectedDoctor.getFullName());
            bookData.put("specialtyName", currentSelectedDoctor.getSpecialtyName());
            bookData.put("appointmentDate", selectedDate);
            bookData.put("timeSlot", selectedSlot.getSlotTime());
            bookData.put("symptoms", symptoms);
            bookData.put("appointmentType", apptType);
            if (selectedOldRecord != null) {
                bookData.put("previousRecordId", selectedOldRecord.getId());
            }
            bookData.put("fee", currentSelectedDoctor.getConsultationFee());
        } catch (Exception ignored) {}

        TcpClient.sendRequest("BOOK_APPOINTMENT", bookData, new TcpClient.ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                String code = response.optString("appointmentCode", DateTimeUtils.generateAppointmentCode());
                showSuccessBookingDialog(code, currentSelectedDoctor.getFullName(), selectedDate, selectedSlot.getSlotTime());
            }

            @Override
            public void onError(String errorMessage) {
                Appointment appt = new Appointment();
                appt.setAppointmentCode(DateTimeUtils.generateAppointmentCode());
                appt.setPatientId(patientId);
                appt.setPatientName(sessionManager.getFullName());
                appt.setPatientPhone(sessionManager.getRole());
                appt.setDoctorId(currentSelectedDoctor.getId());
                appt.setDoctorName(currentSelectedDoctor.getFullName());
                appt.setSpecialtyName(currentSelectedDoctor.getSpecialtyName());
                appt.setAppointmentDate(selectedDate);
                appt.setTimeSlot(selectedSlot.getSlotTime());
                appt.setSymptoms(symptoms);
                appt.setAppointmentType(apptType);
                appt.setStatus(Appointment.STATUS_PENDING);
                appt.setFee(currentSelectedDoctor.getConsultationFee());
                appt.setPaid(false);

                boolean success = appointmentDao.createAppointment(appt);
                if (success) {
                    auditLogDao.log(patientId, sessionManager.getFullName(), User.ROLE_PATIENT, "ĐẶT LỊCH KHÁM",
                            "Đặt lịch thành công mã " + appt.getAppointmentCode() + " với " + currentSelectedDoctor.getFullName() + " ngày " + selectedDate);
                    showSuccessBookingDialog(appt.getAppointmentCode(), appt.getDoctorName(), appt.getAppointmentDate(), appt.getTimeSlot());
                } else {
                    Toast.makeText(BookAppointmentActivity.this, "Có lỗi xảy ra: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showSuccessBookingDialog(String code, String doctorName, String date, String timeSlot) {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Đặt Lịch Khám Thành Công!")
                .setMessage("• Mã lịch hẹn: " + code + "\n" +
                        "• Bác sĩ: " + doctorName + "\n" +
                        "• Ngày khám: " + date + "\n" +
                        "• Khung giờ: " + timeSlot + "\n\n" +
                        "[Giao thức TCP/IP]: Server đã lưu lịch hẹn và cấp mã số thành công!\nVui lòng đến trước 15 phút để làm thủ tục tiếp nhận.")
                .setPositiveButton("Xem lịch hẹn của tôi", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
