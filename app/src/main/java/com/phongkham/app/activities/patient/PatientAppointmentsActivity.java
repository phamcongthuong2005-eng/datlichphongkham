package com.phongkham.app.activities.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.AppointmentAdapter;
import com.phongkham.app.business.BusinessRulesValidator;
import com.phongkham.app.database.AppointmentDao;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PatientAppointmentsActivity extends AppCompatActivity {
    private AppointmentDao appointmentDao;
    private AuditLogDao auditLogDao;
    private BusinessRulesValidator rulesValidator;
    private SessionManager sessionManager;

    private ImageView ivBack;
    private TextView tvEmptyState;
    private RecyclerView rvPatientAppointments;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        appointmentDao = new AppointmentDao(this);
        auditLogDao = new AuditLogDao(this);
        rulesValidator = new BusinessRulesValidator(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadAppointments();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvPatientAppointments = findViewById(R.id.rvPatientAppointments);

        ivBack.setOnClickListener(v -> finish());
        rvPatientAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAppointments() {
        int patientId = sessionManager.getUserId();
        appointmentList = appointmentDao.getAppointmentsByPatient(patientId);

        if (appointmentList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvPatientAppointments.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvPatientAppointments.setVisibility(View.VISIBLE);

            adapter = new AppointmentAdapter(this, appointmentList, User.ROLE_PATIENT, new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onPrimaryAction(Appointment appointment) {
                    // Xem kết quả khám / đơn thuốc nếu COMPLETED
                    if (Appointment.STATUS_COMPLETED.equals(appointment.getStatus())) {
                        Intent intent = new Intent(PatientAppointmentsActivity.this, MedicalRecordDetailActivity.class);
                        intent.putExtra("appointment", appointment);
                        startActivity(intent);
                    } else {
                        showAppointmentDetailDialog(appointment);
                    }
                }

                @Override
                public void onSecondaryAction(Appointment appointment) {
                    if (Appointment.STATUS_COMPLETED.equals(appointment.getStatus())) {
                        // Đánh giá bác sĩ
                        Intent intent = new Intent(PatientAppointmentsActivity.this, ReviewDoctorActivity.class);
                        intent.putExtra("appointment", appointment);
                        startActivity(intent);
                    } else {
                        // Hủy lịch
                        handleCancelAppointment(appointment);
                    }
                }

                @Override
                public void onItemClick(Appointment appointment) {
                    showAppointmentDetailDialog(appointment);
                }
            });
            rvPatientAppointments.setAdapter(adapter);
        }
    }

    private void handleCancelAppointment(Appointment appt) {
        // Business Rule: Kiểm tra thời gian hủy trước giờ hẹn (ít nhất 2 tiếng)
        if (!rulesValidator.canCancelOrReschedule(appt.getAppointmentDate(), appt.getTimeSlot())) {
            int minHours = rulesValidator.getMinHoursBeforeCancel();
            new AlertDialog.Builder(this)
                    .setTitle("Không thể hủy lịch")
                    .setMessage("Theo quy định của phòng khám, bạn chỉ có thể hủy lịch trước giờ khám tối thiểu " + minHours + " tiếng.\nHiện tại đã quá hạn hủy trực tuyến, vui lòng liên hệ trực tiếp hotline phòng khám để được hỗ trợ!")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy lịch khám")
                .setMessage("Bạn có chắc chắn muốn hủy lịch khám mã " + appt.getAppointmentCode() + " không?\n(Khung giờ sẽ được giải phóng cho bệnh nhân khác)")
                .setPositiveButton("Hủy lịch này", (dialog, which) -> {
                    boolean ok = appointmentDao.updateStatus(appt.getId(), Appointment.STATUS_CANCELLED);
                    if (ok) {
                        auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_PATIENT,
                                "HỦY LỊCH KHÁM", "Bệnh nhân đã hủy lịch hẹn mã " + appt.getAppointmentCode());
                        Toast.makeText(PatientAppointmentsActivity.this, "Đã hủy lịch hẹn thành công!", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    }
                })
                .setNegativeButton("Giữ lại lịch", null)
                .show();
    }

    private void showAppointmentDetailDialog(Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lịch hẹn: " + appt.getAppointmentCode())
                .setMessage("• Bác sĩ: " + appt.getDoctorName() + " (" + appt.getSpecialtyName() + ")\n" +
                        "• Ngày khám: " + appt.getAppointmentDate() + "\n" +
                        "• Khung giờ: " + appt.getTimeSlot() + "\n" +
                        "• Trạng thái: " + appt.getStatus() + "\n" +
                        "• Số thứ tự tiếp nhận: #" + (appt.getQueueNumber() > 0 ? appt.getQueueNumber() : "Chưa cấp") + "\n" +
                        "• Triệu chứng: " + appt.getSymptoms() + "\n" +
                        "• Thời gian đặt: " + appt.getCreatedAt())
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }
}
