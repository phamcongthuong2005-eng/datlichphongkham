package com.phongkham.app.activities.doctor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DoctorDao;
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

public class DoctorSlotToggleActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DoctorDao doctorDao;
    private AuditLogDao auditLogDao;

    private ImageView ivBack;
    private EditText etToggleDate;
    private Button btnCloseAllDay, btnOpenAllDay, btnAddCustomSlot;
    private TextView tvToggleSummary;
    private RecyclerView rvToggleSlots;

    private Doctor currentDoctor;
    private String selectedDate = "";
    private List<TimeSlot> slotList = new ArrayList<>();
    private SlotToggleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_slot_toggle);

        sessionManager = new SessionManager(this);
        doctorDao = new DoctorDao(this);
        auditLogDao = new AuditLogDao(this);

        selectedDate = DateTimeUtils.getToday();

        initViews();
        loadDoctorProfile();
        setupDatePicker();
        setupActions();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etToggleDate = findViewById(R.id.etToggleDate);
        btnCloseAllDay = findViewById(R.id.btnCloseAllDay);
        btnOpenAllDay = findViewById(R.id.btnOpenAllDay);
        btnAddCustomSlot = findViewById(R.id.btnAddCustomSlot);
        tvToggleSummary = findViewById(R.id.tvToggleSummary);
        rvToggleSlots = findViewById(R.id.rvToggleSlots);

        ivBack.setOnClickListener(v -> finish());
        etToggleDate.setText(selectedDate);
        rvToggleSlots.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadDoctorProfile() {
        int userId = sessionManager.getUserId();
        currentDoctor = doctorDao.getDoctorByUserId(userId);
        if (currentDoctor == null) {
            List<Doctor> all = doctorDao.getAllDoctors();
            if (!all.isEmpty()) currentDoctor = all.get(0);
        }
        loadSlots();
    }

    private void setupDatePicker() {
        etToggleDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(cal.getTime());
                etToggleDate.setText(selectedDate);
                loadSlots();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void loadSlots() {
        if (currentDoctor == null) return;

        boolean isOnLeave = doctorDao.isDoctorOnLeave(currentDoctor.getId(), selectedDate);
        slotList = doctorDao.getTimeSlots(currentDoctor.getId(), selectedDate);
        if (slotList.isEmpty()) {
            String[] standardSlots = {
                    "08:00 - 08:30", "08:30 - 09:00", "09:00 - 09:30", "09:30 - 10:00",
                    "10:00 - 10:30", "10:30 - 11:00", "11:00 - 11:30", "11:30 - 12:00",
                    "15:00 - 15:30", "15:30 - 16:00", "16:00 - 16:30", "16:30 - 17:00",
                    "17:00 - 17:30", "17:30 - 18:00", "18:00 - 18:30", "18:30 - 19:00",
                    "19:00 - 19:30", "19:30 - 20:00"
            };
            for (String slotStr : standardSlots) {
                slotList.add(new TimeSlot(0, currentDoctor.getId(), selectedDate, slotStr, 3, 0));
            }
        }

        if (isOnLeave) {
            tvToggleSummary.setText("⚠️ Ngày " + selectedDate + " đang trong trạng thái TẠM ĐÓNG NHẬN BỆNH (Nghỉ phép).");
        } else {
            tvToggleSummary.setText("Đang quản lý ca khám ngày " + selectedDate + " (" + slotList.size() + " khung giờ):");
        }

        adapter = new SlotToggleAdapter();
        rvToggleSlots.setAdapter(adapter);
    }

    private void setupActions() {
        btnCloseAllDay.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tạm đóng cả ngày")
                    .setMessage("Bạn có chắc muốn đóng nhận bệnh toàn bộ ca ngày " + selectedDate + "?")
                    .setPositiveButton("Đồng ý đóng", (dialog, which) -> {
                        doctorDao.setDayAvailability(currentDoctor.getId(), selectedDate, false);
                        auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                                "ĐÓNG CA KHÁM NGÀY", "Bác sĩ tạm đóng nhận bệnh ngày " + selectedDate);
                        Toast.makeText(this, "Đã tạm đóng nhận khám ngày " + selectedDate, Toast.LENGTH_SHORT).show();
                        loadSlots();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnOpenAllDay.setOnClickListener(v -> {
            doctorDao.setDayAvailability(currentDoctor.getId(), selectedDate, true);
            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_DOCTOR,
                    "MỞ LẠI CA KHÁM NGÀY", "Bác sĩ mở lại ca khám ngày " + selectedDate);
            Toast.makeText(this, "Đã mở lại toàn bộ ca khám ngày " + selectedDate, Toast.LENGTH_SHORT).show();
            loadSlots();
        });

        btnAddCustomSlot.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
                String startTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                int endMinute = (selectedMinute + 30) % 60;
                int endHour = selectedHour + ((selectedMinute + 30) / 60);
                String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);
                String customSlotTime = startTime + " - " + endTime;
                doctorDao.addCustomSlot(currentDoctor.getId(), selectedDate, customSlotTime);
                Toast.makeText(this, "Đã thêm giờ khám lẻ: " + customSlotTime, Toast.LENGTH_SHORT).show();
                loadSlots();
            }, hour, minute, true);
            timePicker.setTitle("Chọn giờ bắt đầu khám lẻ");
            timePicker.show();
        });
    }

    class SlotToggleAdapter extends RecyclerView.Adapter<SlotToggleAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_slot_toggle, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TimeSlot slot = slotList.get(position);
            holder.tvSlotTime.setText(slot.getSlotTime());

            boolean isAvail = slot.isAvailable();
            holder.switchSlotAvailability.setOnCheckedChangeListener(null);
            holder.switchSlotAvailability.setChecked(isAvail);

            if (isAvail) {
                holder.tvSlotStatusText.setText("Đang mở nhận bệnh (Đã đặt: " + slot.getBookedPatients() + "/3)");
                holder.tvSlotStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.tvSlotStatusText.setText("Đã tạm đóng (Không nhận đặt lịch)");
                holder.tvSlotStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            holder.switchSlotAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                slot.setAvailable(isChecked);
                doctorDao.toggleSlotAvailability(currentDoctor.getId(), selectedDate, slot.getSlotTime(), isChecked);
                if (isChecked) {
                    holder.tvSlotStatusText.setText("Đang mở nhận bệnh (Đã đặt: " + slot.getBookedPatients() + "/3)");
                    holder.tvSlotStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    holder.tvSlotStatusText.setText("Đã tạm đóng (Không nhận đặt lịch)");
                    holder.tvSlotStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            });
        }

        @Override
        public int getItemCount() {
            return slotList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlotTime, tvSlotStatusText;
            SwitchCompat switchSlotAvailability;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSlotTime = itemView.findViewById(R.id.tvSlotTime);
                tvSlotStatusText = itemView.findViewById(R.id.tvSlotStatusText);
                switchSlotAvailability = itemView.findViewById(R.id.switchSlotAvailability);
            }
        }
    }
}
