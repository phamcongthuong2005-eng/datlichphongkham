package com.phongkham.app.activities.staff;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.phongkham.app.database.UserDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.TimeSlot;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StaffCreateBookingActivity extends AppCompatActivity {
    private ImageView ivBack;
    private EditText etStaffPatientName, etStaffPatientPhone, etStaffSelectDate, etStaffSymptoms;
    private Spinner spinnerStaffDoctor;
    private RecyclerView rvStaffSlots;
    private Button btnStaffSubmitBooking;

    private DoctorDao doctorDao;
    private AppointmentDao appointmentDao;
    private UserDao userDao;
    private AuditLogDao auditLogDao;
    private BusinessRulesValidator rulesValidator;
    private SessionManager sessionManager;

    private List<Doctor> doctorList = new ArrayList<>();
    private List<TimeSlot> slotList = new ArrayList<>();
    private TimeSlotAdapter slotAdapter;
    private Doctor selectedDoctor;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_create_booking);

        doctorDao = new DoctorDao(this);
        appointmentDao = new AppointmentDao(this);
        userDao = new UserDao(this);
        auditLogDao = new AuditLogDao(this);
        rulesValidator = new BusinessRulesValidator(this);
        sessionManager = new SessionManager(this);

        selectedDate = DateTimeUtils.getToday();

        initViews();
        setupDoctors();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etStaffPatientName = findViewById(R.id.etStaffPatientName);
        etStaffPatientPhone = findViewById(R.id.etStaffPatientPhone);
        etStaffSelectDate = findViewById(R.id.etStaffSelectDate);
        etStaffSymptoms = findViewById(R.id.etStaffSymptoms);
        spinnerStaffDoctor = findViewById(R.id.spinnerStaffDoctor);
        rvStaffSlots = findViewById(R.id.rvStaffSlots);
        btnStaffSubmitBooking = findViewById(R.id.btnStaffSubmitBooking);

        etStaffSelectDate.setText(selectedDate);
        rvStaffSlots.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupDoctors() {
        doctorList = doctorDao.getAllDoctors();
        List<String> docNames = new ArrayList<>();
        for (Doctor d : doctorList) {
            docNames.add(d.getFullName() + " (" + d.getSpecialtyName() + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, docNames);
        spinnerStaffDoctor.setAdapter(adapter);

        spinnerStaffDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < doctorList.size()) {
                    selectedDoctor = doctorList.get(position);
                    loadTimeSlots();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDatePicker() {
        etStaffSelectDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(cal.getTime());
                etStaffSelectDate.setText(selectedDate);
                loadTimeSlots();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
    }

    private void loadTimeSlots() {
        if (selectedDoctor == null) return;
        slotList = doctorDao.getTimeSlots(selectedDoctor.getId(), selectedDate);
        if (slotList.isEmpty()) {
            String[] standardSlots = {
                    "08:00 - 08:30", "08:30 - 09:00", "09:00 - 09:30", "09:30 - 10:00",
                    "10:00 - 10:30", "10:30 - 11:00", "11:00 - 11:30", "11:30 - 12:00",
                    "15:00 - 15:30", "15:30 - 16:00", "16:00 - 16:30", "16:30 - 17:00",
                    "17:00 - 17:30", "17:30 - 18:00", "18:00 - 18:30", "18:30 - 19:00",
                    "19:00 - 19:30", "19:30 - 20:00"
            };
            for (String slotStr : standardSlots) {
                slotList.add(new TimeSlot(0, selectedDoctor.getId(), selectedDate, slotStr, 3, 0));
            }
        }
        slotAdapter = new TimeSlotAdapter(this, slotList, slot -> {});
        rvStaffSlots.setAdapter(slotAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnStaffSubmitBooking.setOnClickListener(v -> handleCreateBooking());
    }

    private void handleCreateBooking() {
        String patientName = etStaffPatientName.getText().toString().trim();
        String patientPhone = etStaffPatientPhone.getText().toString().trim();
        String symptoms = etStaffSymptoms.getText().toString().trim();

        if (patientName.isEmpty() || patientPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên và số điện thoại bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlot selectedSlot = slotAdapter != null ? slotAdapter.getSelectedSlot() : null;
        if (selectedSlot == null) {
            Toast.makeText(this, "Vui lòng chọn khung giờ khám còn trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find or create Patient User
        List<User> found = userDao.searchPatients(patientPhone);
        int patientId;
        if (!found.isEmpty()) {
            patientId = found.get(0).getId();
        } else {
            User newUser = new User();
            newUser.setFullName(patientName);
            newUser.setPhone(patientPhone);
            newUser.setEmail(patientPhone + "@phongkham.vn");
            newUser.setPassword("123456");
            newUser.setRole(User.ROLE_PATIENT);
            userDao.register(newUser);
            patientId = newUser.getId();
        }

        Appointment appt = new Appointment();
        appt.setAppointmentCode(DateTimeUtils.generateAppointmentCode());
        appt.setPatientId(patientId);
        appt.setPatientName(patientName);
        appt.setPatientPhone(patientPhone);
        appt.setDoctorId(selectedDoctor.getId());
        appt.setDoctorName(selectedDoctor.getFullName());
        appt.setSpecialtyName(selectedDoctor.getSpecialtyName());
        appt.setAppointmentDate(selectedDate);
        appt.setTimeSlot(selectedSlot.getSlotTime());
        appt.setSymptoms(symptoms.isEmpty() ? "Đặt lịch trực tiếp tại quầy" : symptoms);
        appt.setStatus(Appointment.STATUS_CONFIRMED); // Staff created can be confirmed right away
        appt.setFee(selectedDoctor.getConsultationFee());
        appt.setPaid(false);

        boolean ok = appointmentDao.createAppointment(appt);
        if (ok) {
            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_STAFF,
                    "ĐẶT LỊCH HỘ", "Lễ tân tạo lịch hẹn " + appt.getAppointmentCode() + " cho bệnh nhân " + patientName);

            new AlertDialog.Builder(this)
                    .setTitle("🎉 Tạo lịch hẹn thành công!")
                    .setMessage("Mã lịch hẹn: " + appt.getAppointmentCode() + "\n" +
                            "Bệnh nhân: " + patientName + " (" + patientPhone + ")\n" +
                            "Bác sĩ: " + appt.getDoctorName() + "\n" +
                            "Thời gian: " + appt.getAppointmentDate() + " • " + appt.getTimeSlot())
                    .setPositiveButton("Hoàn tất", (dialog, which) -> finish())
                    .show();
        }
    }
}
