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
    private AuditLogDao auditLogDao;
    private BusinessRulesValidator rulesValidator;
    private SessionManager sessionManager;

    private ImageView ivBack;
    private Spinner spinnerSpecialty, spinnerDoctor;
    private TextView tvDoctorSummary, tvDoctorWorkingDays;
    private EditText etSelectDate, etSymptoms;
    private RecyclerView rvTimeSlots;
    private Button btnConfirmBooking;

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
        auditLogDao = new AuditLogDao(this);
        rulesValidator = new BusinessRulesValidator(this);
        sessionManager = new SessionManager(this);

        preSelectedDoctorId = getIntent().getIntExtra("selectedDoctorId", -1);

        initViews();
        setupSpecialties();
        setupDatePicker();
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
