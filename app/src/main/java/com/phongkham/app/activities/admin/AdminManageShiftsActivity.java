package com.phongkham.app.activities.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.ShiftAdapter;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.ShiftDao;
import com.phongkham.app.models.ShiftAssignment;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AdminManageShiftsActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView btnAddShift, tvEmptyShifts;
    private RecyclerView rvShifts;

    private ShiftDao shiftDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;

    private List<ShiftAssignment> shiftList = new ArrayList<>();
    private ShiftAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_shifts);

        shiftDao = new ShiftDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadShifts();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddShift = findViewById(R.id.btnAddShift);
        tvEmptyShifts = findViewById(R.id.tvEmptyShifts);
        rvShifts = findViewById(R.id.rvShifts);

        rvShifts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadShifts() {
        String today = DateTimeUtils.getToday();
        shiftList = shiftDao.getShiftsByDate(today);

        if (shiftList.isEmpty()) {
            tvEmptyShifts.setVisibility(View.VISIBLE);
            rvShifts.setVisibility(View.GONE);
        } else {
            tvEmptyShifts.setVisibility(View.GONE);
            rvShifts.setVisibility(View.VISIBLE);

            adapter = new ShiftAdapter(this, shiftList, shift -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa ca phân công")
                        .setMessage("Bạn có chắc muốn xóa ca làm của " + shift.getTenBacSi() + " tại " + shift.getTenPhong() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean ok = shiftDao.deleteShift(shift.getId());
                            if (ok) {
                                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                                        "HỦY PHÂN CA", "Admin hủy phân ca làm việc ID: " + shift.getId());
                                Toast.makeText(this, "Đã xóa ca phân công!", Toast.LENGTH_SHORT).show();
                                loadShifts();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
            rvShifts.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddShift.setOnClickListener(v -> showAddShiftDialog());
    }

    private void showAddShiftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Phân ca trực & Phân công Y tá (Menu 5.4)");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_shift, null);
        EditText etShiftName = view.findViewById(R.id.etShiftName);
        EditText etShiftDate = view.findViewById(R.id.etShiftDate);
        EditText etStartTime = view.findViewById(R.id.etStartTime);
        EditText etEndTime = view.findViewById(R.id.etEndTime);
        EditText etShiftRoomCode = view.findViewById(R.id.etShiftRoomCode);
        EditText etShiftDoctorName = view.findViewById(R.id.etShiftDoctorName);
        EditText etShiftNurseName = view.findViewById(R.id.etShiftNurseName);

        etShiftDate.setText(DateTimeUtils.getToday());
        etStartTime.setText("07:30");
        etEndTime.setText("11:30");

        builder.setView(view);

        builder.setPositiveButton("Lưu phân ca", (dialog, which) -> {
            String name = etShiftName.getText().toString().trim();
            String date = etShiftDate.getText().toString().trim();
            String start = etStartTime.getText().toString().trim();
            String end = etEndTime.getText().toString().trim();
            String room = etShiftRoomCode.getText().toString().trim();
            String doctor = etShiftDoctorName.getText().toString().trim();
            String nurse = etShiftNurseName.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || doctor.isEmpty()) {
                Toast.makeText(AdminManageShiftsActivity.this, "Vui lòng nhập đầy đủ thông tin ca trực!", Toast.LENGTH_SHORT).show();
                return;
            }

            ShiftAssignment s = new ShiftAssignment();
            s.setMaCa(name);
            s.setNgayLam(date);
            s.setGioBatDau(start);
            s.setGioKetThuc(end);
            s.setMaPhong(room.contains("-") ? room.split("-")[0].trim() : "P101");
            s.setTenPhong(room);
            s.setMaBacSi(2);
            s.setTenBacSi(doctor);
            s.setMaYTa(4);
            s.setTenYTa(nurse.isEmpty() ? "ĐD. Phụ trách chung" : nurse);
            s.setTrangThai("Hoạt động");

            boolean ok = shiftDao.addShift(s);
            if (ok) {
                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                        "PHÂN CA LÀM VIỆC", "Admin phân ca '" + name + "' cho BS " + doctor + " & Y tá " + nurse + " tại " + room);
                Toast.makeText(AdminManageShiftsActivity.this, "Phân ca làm việc thành công!", Toast.LENGTH_SHORT).show();
                loadShifts();
            } else {
                Toast.makeText(AdminManageShiftsActivity.this, "Phân ca thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
