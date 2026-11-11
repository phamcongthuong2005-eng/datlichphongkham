package com.phongkham.app.activities.patient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.InvoiceDao;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Review;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

public class ReviewDoctorActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvReviewDoctorInfo;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmitReview;

    private InvoiceDao invoiceDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private Appointment appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_doctor);

        invoiceDao = new InvoiceDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);
        appointment = (Appointment) getIntent().getSerializableExtra("appointment");

        initViews();
        displayData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvReviewDoctorInfo = findViewById(R.id.tvReviewDoctorInfo);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
    }

    private void displayData() {
        if (appointment != null) {
            tvReviewDoctorInfo.setText("Đánh giá buổi khám với: " + appointment.getDoctorName() + " (" + appointment.getSpecialtyName() + ")\nNgày khám: " + appointment.getAppointmentDate());
            
            // Check if already reviewed
            Review existing = invoiceDao.getReviewByAppointmentId(appointment.getId());
            if (existing != null) {
                ratingBar.setRating(existing.getRating());
                etComment.setText(existing.getComment());
                btnSubmitReview.setText("CẬP NHẬT ĐÁNH GIÁ");
            }
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSubmitReview.setOnClickListener(v -> {
            if (appointment == null) return;
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            Review r = new Review();
            r.setAppointmentId(appointment.getId());
            r.setPatientId(sessionManager.getUserId());
            r.setPatientName(sessionManager.getFullName());
            r.setDoctorId(appointment.getDoctorId());
            r.setRating(rating);
            r.setComment(comment);

            boolean ok = invoiceDao.addReview(r);
            if (ok) {
                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_PATIENT,
                        "ĐÁNH GIÁ DỊCH VỤ", "Đánh giá " + rating + " sao cho buổi khám " + appointment.getAppointmentCode());
                Toast.makeText(this, "Cảm ơn bạn đã gửi đánh giá và đóng góp ý kiến!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Không thể lưu đánh giá. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
