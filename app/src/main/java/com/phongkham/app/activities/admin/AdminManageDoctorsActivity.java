package com.phongkham.app.activities.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.DoctorAdapter;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.Specialty;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AdminManageDoctorsActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvAdminDoctorCount;
    private Button btnAddNewDoctor;
    private RecyclerView rvAdminDoctors;

    private DoctorDao doctorDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private List<Doctor> doctorList = new ArrayList<>();
    private DoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_doctors);

        doctorDao = new DoctorDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadDoctors();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvAdminDoctorCount = findViewById(R.id.tvAdminDoctorCount);
        btnAddNewDoctor = findViewById(R.id.btnAddNewDoctor);
        rvAdminDoctors = findViewById(R.id.rvAdminDoctors);

        ivBack.setOnClickListener(v -> finish());
        rvAdminDoctors.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadDoctors() {
        doctorList = doctorDao.getAllDoctors();
        tvAdminDoctorCount.setText("Danh sách bác sĩ trực thuộc phòng khám (" + doctorList.size() + "):");

        adapter = new DoctorAdapter(this, doctorList, new DoctorAdapter.OnDoctorClickListener() {
            @Override
            public void onDoctorClick(Doctor doctor) {
                showDoctorOptions(doctor);
            }

            @Override
            public void onBookClick(Doctor doctor) {
                showDoctorOptions(doctor);
            }
        });
        rvAdminDoctors.setAdapter(adapter);
    }

    private void showDoctorOptions(Doctor doctor) {
        String[] options = {"Sửa thông tin bác sĩ", "Xóa bác sĩ khỏi phòng khám", "Đóng"};
        new AlertDialog.Builder(this)
                .setTitle(doctor.getFullName() + " (" + doctor.getSpecialtyName() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDoctorDialog(doctor);
                    } else if (which == 1) {
                        handleDeleteDoctor(doctor);
                    }
                })
                .show();
    }

    private void handleDeleteDoctor(Doctor doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bác sĩ")
                .setMessage("Bạn có chắc chắn muốn xóa " + doctor.getFullName() + " khỏi danh sách?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean ok = doctorDao.deleteDoctor(doctor.getId());
                    if (ok) {
                        auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                                "XÓA BÁC SĨ", "Admin đã xóa bác sĩ " + doctor.getFullName());
                        Toast.makeText(this, "Đã xóa bác sĩ thành công!", Toast.LENGTH_SHORT).show();
                        loadDoctors();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupListeners() {
        btnAddNewDoctor.setOnClickListener(v -> showAddDoctorDialog());
    }

    private void showAddDoctorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Bác Sĩ Mới");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        EditText etDocName = view.findViewById(R.id.etDocName);
        Spinner spinnerDocSpecialty = view.findViewById(R.id.spinnerDocSpecialty);
        EditText etDocDegree = view.findViewById(R.id.etDocDegree);
        EditText etDocExp = view.findViewById(R.id.etDocExp);
        EditText etDocFee = view.findViewById(R.id.etDocFee);
        EditText etDocBio = view.findViewById(R.id.etDocBio);

        List<Specialty> specs = doctorDao.getAllSpecialties();
        List<String> specNames = new ArrayList<>();
        for (Specialty s : specs) specNames.add(s.getName());
        ArrayAdapter<String> specAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specNames);
        spinnerDocSpecialty.setAdapter(specAdapter);

        builder.setView(view);
        builder.setPositiveButton("Lưu Bác Sĩ", (dialog, which) -> {
            String name = etDocName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Họ tên bác sĩ không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            int specPos = spinnerDocSpecialty.getSelectedItemPosition();
            Specialty selSpec = specs.get(specPos);

            int exp = 5;
            try { exp = Integer.parseInt(etDocExp.getText().toString().trim()); } catch (Exception ignored) {}
            double fee = 200000;
            try { fee = Double.parseDouble(etDocFee.getText().toString().trim()); } catch (Exception ignored) {}

            Doctor newDoc = new Doctor(0, 0, name, selSpec.getId(), selSpec.getName(),
                    etDocDegree.getText().toString().trim(), exp, fee, 5.0f, etDocBio.getText().toString().trim());

            boolean ok = doctorDao.addDoctor(newDoc);
            if (ok) {
                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                        "THÊM BÁC SĨ", "Admin đã thêm mới bác sĩ: " + name + " (Chuyên khoa: " + selSpec.getName() + ")");
                Toast.makeText(this, "Thêm bác sĩ thành công!", Toast.LENGTH_SHORT).show();
                loadDoctors();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showEditDoctorDialog(Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa Bác Sĩ: " + doctor.getFullName());

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        EditText etDocName = view.findViewById(R.id.etDocName);
        Spinner spinnerDocSpecialty = view.findViewById(R.id.spinnerDocSpecialty);
        EditText etDocDegree = view.findViewById(R.id.etDocDegree);
        EditText etDocExp = view.findViewById(R.id.etDocExp);
        EditText etDocFee = view.findViewById(R.id.etDocFee);
        EditText etDocBio = view.findViewById(R.id.etDocBio);

        etDocName.setText(doctor.getFullName());
        etDocDegree.setText(doctor.getDegree());
        etDocExp.setText(String.valueOf(doctor.getExperienceYears()));
        etDocFee.setText(String.valueOf((int) doctor.getConsultationFee()));
        etDocBio.setText(doctor.getBio());

        List<Specialty> specs = doctorDao.getAllSpecialties();
        List<String> specNames = new ArrayList<>();
        int selIdx = 0;
        for (int i = 0; i < specs.size(); i++) {
            specNames.add(specs.get(i).getName());
            if (specs.get(i).getId() == doctor.getSpecialtyId()) selIdx = i;
        }
        ArrayAdapter<String> specAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specNames);
        spinnerDocSpecialty.setAdapter(specAdapter);
        spinnerDocSpecialty.setSelection(selIdx);

        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            doctor.setFullName(etDocName.getText().toString().trim());
            int specPos = spinnerDocSpecialty.getSelectedItemPosition();
            Specialty selSpec = specs.get(specPos);
            doctor.setSpecialtyId(selSpec.getId());
            doctor.setSpecialtyName(selSpec.getName());
            doctor.setDegree(etDocDegree.getText().toString().trim());
            try { doctor.setExperienceYears(Integer.parseInt(etDocExp.getText().toString().trim())); } catch (Exception ignored) {}
            try { doctor.setConsultationFee(Double.parseDouble(etDocFee.getText().toString().trim())); } catch (Exception ignored) {}
            doctor.setBio(etDocBio.getText().toString().trim());

            boolean ok = doctorDao.updateDoctor(doctor);
            if (ok) {
                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                        "CẬP NHẬT BÁC SĨ", "Admin đã cập nhật thông tin bác sĩ: " + doctor.getFullName());
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                loadDoctors();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
