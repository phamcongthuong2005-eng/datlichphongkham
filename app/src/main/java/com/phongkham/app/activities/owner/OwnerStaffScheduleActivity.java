package com.phongkham.app.activities.owner;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.database.DoctorDao;
import com.phongkham.app.database.ShiftDao;
import com.phongkham.app.database.UserDao;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.ShiftAssignment;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OwnerStaffScheduleActivity extends AppCompatActivity {
    private ImageView ivBack;
    private EditText etScheduleDate;
    private Button btnScheduleAll, btnScheduleDoctors, btnScheduleStaff, btnScheduleLeave;
    private TextView tvCountDoctorOnDuty, tvCountStaffOnDuty, tvCountStaffOnLeave;
    private RecyclerView rvStaffSchedule;

    private DoctorDao doctorDao;
    private UserDao userDao;
    private ShiftDao shiftDao;

    private String selectedDate = "";
    private int currentFilter = 0; // 0: All, 1: Doctors, 2: Staff/Nurse, 3: Leave

    private static class ScheduleItem {
        String role;
        String name;
        String shiftTime;
        String room;
        String phone;
        String note;
        boolean isOnLeave;
    }

    private final List<ScheduleItem> allItems = new ArrayList<>();
    private final List<ScheduleItem> displayItems = new ArrayList<>();
    private StaffScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_staff_schedule);

        doctorDao = new DoctorDao(this);
        userDao = new UserDao(this);
        shiftDao = new ShiftDao(this);

        selectedDate = DateTimeUtils.getToday();

        initViews();
        setupDatePicker();
        setupFilters();
        loadScheduleData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etScheduleDate = findViewById(R.id.etScheduleDate);
        btnScheduleAll = findViewById(R.id.btnScheduleAll);
        btnScheduleDoctors = findViewById(R.id.btnScheduleDoctors);
        btnScheduleStaff = findViewById(R.id.btnScheduleStaff);
        btnScheduleLeave = findViewById(R.id.btnScheduleLeave);

        tvCountDoctorOnDuty = findViewById(R.id.tvCountDoctorOnDuty);
        tvCountStaffOnDuty = findViewById(R.id.tvCountStaffOnDuty);
        tvCountStaffOnLeave = findViewById(R.id.tvCountStaffOnLeave);
        rvStaffSchedule = findViewById(R.id.rvStaffSchedule);

        ivBack.setOnClickListener(v -> finish());
        etScheduleDate.setText(selectedDate);
        rvStaffSchedule.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDatePicker() {
        etScheduleDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(cal.getTime());
                etScheduleDate.setText(selectedDate);
                loadScheduleData();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void setupFilters() {
        btnScheduleAll.setOnClickListener(v -> {
            currentFilter = 0;
            updateFilterButtons();
            applyFilter();
        });

        btnScheduleDoctors.setOnClickListener(v -> {
            currentFilter = 1;
            updateFilterButtons();
            applyFilter();
        });

        btnScheduleStaff.setOnClickListener(v -> {
            currentFilter = 2;
            updateFilterButtons();
            applyFilter();
        });

        btnScheduleLeave.setOnClickListener(v -> {
            currentFilter = 3;
            updateFilterButtons();
            applyFilter();
        });
    }

    private void updateFilterButtons() {
        btnScheduleAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 0 ? 0xFF0284C7 : 0xFFF1F5F9));
        btnScheduleAll.setTextColor(currentFilter == 0 ? 0xFFFFFFFF : 0xFF334155);

        btnScheduleDoctors.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 1 ? 0xFF0284C7 : 0xFFF1F5F9));
        btnScheduleDoctors.setTextColor(currentFilter == 1 ? 0xFFFFFFFF : 0xFF334155);

        btnScheduleStaff.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 2 ? 0xFF0284C7 : 0xFFF1F5F9));
        btnScheduleStaff.setTextColor(currentFilter == 2 ? 0xFFFFFFFF : 0xFF334155);

        btnScheduleLeave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 3 ? 0xFF0284C7 : 0xFFF1F5F9));
        btnScheduleLeave.setTextColor(currentFilter == 3 ? 0xFFFFFFFF : 0xFF334155);
    }

    private void loadScheduleData() {
        allItems.clear();

        List<Doctor> doctors = doctorDao.getAllDoctors();
        List<ShiftAssignment> shifts = shiftDao.getShiftsByDate(selectedDate);
        List<User> users = userDao.getAllUsers();

        int docOnDuty = 0;
        int staffOnDuty = 0;
        int onLeaveCount = 0;

        // 1. Nạp lịch trình Bác sĩ
        int roomIdx = 101;
        for (Doctor doc : doctors) {
            ScheduleItem item = new ScheduleItem();
            item.role = "BÁC SĨ";
            item.name = (doc.getDegree() != null ? doc.getDegree() + " " : "") + doc.getFullName();
            item.phone = "090000000" + doc.getId();
            item.room = "Phòng " + (roomIdx++) + " - " + doc.getSpecialtyName();
            item.shiftTime = "Ca Trực: 08:00 - 12:00 & 15:00 - 20:00";

            boolean onLeave = doctorDao.isDoctorOnLeave(doc.getId(), selectedDate);
            item.isOnLeave = onLeave;
            if (onLeave) {
                item.note = "⛔ Đã đăng ký nghỉ phép ngày này";
                onLeaveCount++;
            } else {
                item.note = "Khám chữa bệnh chuyên khoa Cơ Xương Khớp theo lịch trực";
                docOnDuty++;
            }
            allItems.add(item);
        }

        // 2. Nạp lịch trình Lễ tân & Y tá
        for (User u : users) {
            if (User.ROLE_STAFF.equals(u.getRole())) {
                ScheduleItem item = new ScheduleItem();
                item.role = "LỄ TÂN";
                item.name = u.getFullName();
                item.phone = u.getPhone() != null ? u.getPhone() : "0900000005";
                item.shiftTime = "Ca Hành Chính (07:30 - 17:30)";
                item.room = "Quầy Tiếp Nhận & Điều Phối Bệnh Nhân";
                item.note = "Tiếp đón, điểm danh và điều phối hàng chờ khám";
                item.isOnLeave = false;
                staffOnDuty++;
                allItems.add(item);
            } else if (User.ROLE_NURSE.equals(u.getRole())) {
                ScheduleItem item = new ScheduleItem();
                item.role = "ĐIỀU DƯỠNG";
                item.name = u.getFullName();
                item.phone = u.getPhone() != null ? u.getPhone() : "0900000006";
                item.shiftTime = "Ca Toàn Ngày (08:00 - 17:30)";
                item.room = "Phòng Đo Sinh Hiệu & Hỗ Trợ Bác Sĩ";
                item.note = "Đo huyết áp, nhiệt độ, chiều cao, cân nặng bệnh nhân";
                item.isOnLeave = false;
                staffOnDuty++;
                allItems.add(item);
            }
        }

        // Bổ sung nhân sự lễ tân mặc định nếu danh sách user ít
        if (staffOnDuty < 2) {
            ScheduleItem s1 = new ScheduleItem();
            s1.role = "LỄ TÂN";
            s1.name = "Lê Thị Hương";
            s1.phone = "0900000005";
            s1.shiftTime = "Ca Sáng (07:30 - 12:00)";
            s1.room = "Bàn Tiếp Đón 01 (Cửa chính)";
            s1.note = "Tiếp đón và hướng dẫn bệnh nhân vãng lai";
            s1.isOnLeave = false;
            allItems.add(s1);
            staffOnDuty++;

            ScheduleItem s2 = new ScheduleItem();
            s2.role = "ĐIỀU DƯỠNG";
            s2.name = "Y Tá Hoàng Kim Ngân";
            s2.phone = "0900000006";
            s2.shiftTime = "Ca Sáng - Chiều (08:00 - 16:30)";
            s2.room = "Phòng Đo Sinh Hiệu P100";
            s2.note = "Kiểm tra dấu hiệu sinh tồn trước khi vào phòng khám";
            s2.isOnLeave = false;
            allItems.add(s2);
            staffOnDuty++;
        }

        tvCountDoctorOnDuty.setText(docOnDuty + " bác sĩ");
        tvCountStaffOnDuty.setText(staffOnDuty + " nhân sự");
        tvCountStaffOnLeave.setText(onLeaveCount + " người");

        applyFilter();
    }

    private void applyFilter() {
        displayItems.clear();
        for (ScheduleItem item : allItems) {
            if (currentFilter == 1 && !"BÁC SĨ".equals(item.role)) continue;
            if (currentFilter == 2 && ("BÁC SĨ".equals(item.role))) continue;
            if (currentFilter == 3 && !item.isOnLeave) continue;
            displayItems.add(item);
        }

        adapter = new StaffScheduleAdapter();
        rvStaffSchedule.setAdapter(adapter);
    }

    class StaffScheduleAdapter extends RecyclerView.Adapter<StaffScheduleAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_staff_schedule, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScheduleItem item = displayItems.get(position);
            holder.tvStaffMemberName.setText(item.name);
            holder.tvStaffRoleBadge.setText(item.role);
            holder.tvStaffShiftType.setText(item.shiftTime);
            holder.tvStaffRoomAssigned.setText("📍 " + item.room);
            holder.tvStaffPhoneContact.setText("📞 " + item.phone);
            holder.tvStaffNoteOrReason.setText(item.note);

            if (item.isOnLeave) {
                holder.tvStaffScheduleStatus.setText("Nghỉ phép");
                holder.tvStaffScheduleStatus.setBackgroundColor(0xFFFEE2E2);
                holder.tvStaffScheduleStatus.setTextColor(0xFFDC2626);
                holder.tvStaffRoleBadge.setBackgroundColor(0xFFF3F4F6);
                holder.tvStaffRoleBadge.setTextColor(0xFF6B7280);
            } else {
                holder.tvStaffScheduleStatus.setText("Đang trực");
                holder.tvStaffScheduleStatus.setBackgroundColor(0xFFDCFCE7);
                holder.tvStaffScheduleStatus.setTextColor(0xFF16A34A);

                if ("BÁC SĨ".equals(item.role)) {
                    holder.tvStaffRoleBadge.setBackgroundColor(0xFFE0F2FE);
                    holder.tvStaffRoleBadge.setTextColor(0xFF0369A1);
                } else {
                    holder.tvStaffRoleBadge.setBackgroundColor(0xFFFEF3C7);
                    holder.tvStaffRoleBadge.setTextColor(0xFFD97706);
                }
            }
        }

        @Override
        public int getItemCount() {
            return displayItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStaffRoleBadge, tvStaffShiftType, tvStaffScheduleStatus;
            TextView tvStaffMemberName, tvStaffRoomAssigned, tvStaffPhoneContact, tvStaffNoteOrReason;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStaffRoleBadge = itemView.findViewById(R.id.tvStaffRoleBadge);
                tvStaffShiftType = itemView.findViewById(R.id.tvStaffShiftType);
                tvStaffScheduleStatus = itemView.findViewById(R.id.tvStaffScheduleStatus);
                tvStaffMemberName = itemView.findViewById(R.id.tvStaffMemberName);
                tvStaffRoomAssigned = itemView.findViewById(R.id.tvStaffRoomAssigned);
                tvStaffPhoneContact = itemView.findViewById(R.id.tvStaffPhoneContact);
                tvStaffNoteOrReason = itemView.findViewById(R.id.tvStaffNoteOrReason);
            }
        }
    }
}
