package com.phongkham.app.activities.staff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.InvoiceDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Invoice;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

public class StaffInvoiceActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvInvoicePatient, tvInvoiceDoctor, tvInvoiceCode, tvTotalAmount;
    private EditText etConsultationFee, etMedicineFee, etTestFee;
    private Spinner spinnerPaymentMethod;
    private Button btnConfirmPayment;

    private InvoiceDao invoiceDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private Appointment appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_invoice);

        invoiceDao = new InvoiceDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        appointment = (Appointment) getIntent().getSerializableExtra("appointment");

        initViews();
        displayData();
        setupPaymentMethods();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvInvoicePatient = findViewById(R.id.tvInvoicePatient);
        tvInvoiceDoctor = findViewById(R.id.tvInvoiceDoctor);
        tvInvoiceCode = findViewById(R.id.tvInvoiceCode);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        etConsultationFee = findViewById(R.id.etConsultationFee);
        etMedicineFee = findViewById(R.id.etMedicineFee);
        etTestFee = findViewById(R.id.etTestFee);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
    }

    private void displayData() {
        if (appointment == null) return;
        tvInvoicePatient.setText("Bệnh nhân: " + appointment.getPatientName());
        tvInvoiceDoctor.setText("Bác sĩ khám: " + appointment.getDoctorName() + " (" + appointment.getSpecialtyName() + ")");
        tvInvoiceCode.setText("Mã lịch hẹn: " + appointment.getAppointmentCode());

        double consultFee = appointment.getFee() > 0 ? appointment.getFee() : 200000;
        etConsultationFee.setText(String.valueOf((int) consultFee));
        etMedicineFee.setText("150000");
        etTestFee.setText("0");

        calculateTotal();
    }

    private void setupPaymentMethods() {
        String[] methods = {"Tiền mặt tại quầy", "Chuyển khoản QR Code ngân hàng", "Bảo hiểm Y Tế (BHYT)", "Thẻ ATM / Visa / Mastercard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerPaymentMethod.setAdapter(adapter);
    }

    private void calculateTotal() {
        double consult = parseDouble(etConsultationFee.getText().toString());
        double med = parseDouble(etMedicineFee.getText().toString());
        double test = parseDouble(etTestFee.getText().toString());
        double total = consult + med + test;
        tvTotalAmount.setText(DateTimeUtils.formatCurrency(total));
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etConsultationFee.addTextChangedListener(watcher);
        etMedicineFee.addTextChangedListener(watcher);
        etTestFee.addTextChangedListener(watcher);

        btnConfirmPayment.setOnClickListener(v -> handleConfirmPayment());
    }

    private void handleConfirmPayment() {
        if (appointment == null) return;

        double consult = parseDouble(etConsultationFee.getText().toString());
        double med = parseDouble(etMedicineFee.getText().toString());
        double test = parseDouble(etTestFee.getText().toString());
        double total = consult + med + test;
        String method = spinnerPaymentMethod.getSelectedItem().toString();

        Invoice invoice = new Invoice();
        invoice.setAppointmentId(appointment.getId());
        invoice.setAppointmentCode(appointment.getAppointmentCode());
        invoice.setPatientId(appointment.getPatientId());
        invoice.setPatientName(appointment.getPatientName());
        invoice.setConsultationFee(consult);
        invoice.setMedicineFee(med);
        invoice.setTestFee(test);
        invoice.setTotalAmount(total);
        invoice.setStatus(Invoice.STATUS_PAID);
        invoice.setPaymentMethod(method);

        boolean ok = invoiceDao.createOrUpdateInvoice(invoice);
        if (ok) {
            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_STAFF,
                    "THU PHÍ KHÁM", "Thu viện phí số tiền " + DateTimeUtils.formatCurrency(total) + " cho bệnh nhân " + appointment.getPatientName() + " (Mã " + appointment.getAppointmentCode() + ") qua " + method);

            new AlertDialog.Builder(this)
                    .setTitle("🧾 Đã lập hóa đơn thu phí thành công!")
                    .setMessage("• Mã khám: " + appointment.getAppointmentCode() + "\n" +
                            "• Bệnh nhân: " + appointment.getPatientName() + "\n" +
                            "• Tiền khám: " + DateTimeUtils.formatCurrency(consult) + "\n" +
                            "• Tiền thuốc: " + DateTimeUtils.formatCurrency(med) + "\n" +
                            "• Tiền xét nghiệm: " + DateTimeUtils.formatCurrency(test) + "\n" +
                            "----------------------------------\n" +
                            "• TỔNG CỘNG: " + DateTimeUtils.formatCurrency(total) + "\n" +
                            "• Hình thức: " + method + "\n" +
                            "• Trạng thái: ĐÃ THANH TOÁN")
                    .setPositiveButton("In biên lai & Hoàn tất", (dialog, which) -> finish())
                    .show();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi lưu hóa đơn!", Toast.LENGTH_SHORT).show();
        }
    }
}
