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
import com.phongkham.app.adapters.RoomAdapter;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.ShiftDao;
import com.phongkham.app.models.ClinicRoom;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AdminManageRoomsActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView btnAddRoom, tvEmptyRooms;
    private RecyclerView rvRooms;

    private ShiftDao shiftDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;

    private List<ClinicRoom> roomList = new ArrayList<>();
    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_rooms);

        shiftDao = new ShiftDao(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadRooms();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        tvEmptyRooms = findViewById(R.id.tvEmptyRooms);
        rvRooms = findViewById(R.id.rvRooms);

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRooms() {
        roomList = shiftDao.getAllRooms();
        if (roomList.isEmpty()) {
            tvEmptyRooms.setVisibility(View.VISIBLE);
            rvRooms.setVisibility(View.GONE);
        } else {
            tvEmptyRooms.setVisibility(View.GONE);
            rvRooms.setVisibility(View.VISIBLE);

            adapter = new RoomAdapter(this, roomList, room -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa phòng khám")
                        .setMessage("Bạn có chắc muốn xóa phòng " + room.getMaPhong() + " (" + room.getTenPhong() + ")?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean ok = shiftDao.deleteRoom(room.getMaPhong());
                            if (ok) {
                                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                                        "XÓA PHÒNG KHÁM", "Admin xóa phòng khám: " + room.getMaPhong());
                                Toast.makeText(this, "Đã xóa phòng khám!", Toast.LENGTH_SHORT).show();
                                loadRooms();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
            rvRooms.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddRoom.setOnClickListener(v -> showAddRoomDialog());
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm phòng khám vật lý mới");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        EditText etRoomCode = view.findViewById(R.id.etRoomCode);
        EditText etRoomName = view.findViewById(R.id.etRoomName);
        EditText etRoomType = view.findViewById(R.id.etRoomType);
        builder.setView(view);

        builder.setPositiveButton("Thêm phòng", (dialog, which) -> {
            String code = etRoomCode.getText().toString().trim();
            String name = etRoomName.getText().toString().trim();
            String type = etRoomType.getText().toString().trim();

            if (code.isEmpty() || name.isEmpty()) {
                Toast.makeText(AdminManageRoomsActivity.this, "Vui lòng nhập mã và tên phòng!", Toast.LENGTH_SHORT).show();
                return;
            }

            ClinicRoom newRoom = new ClinicRoom(code, name, type.isEmpty() ? "Khám bệnh" : type, "Hoạt động");
            boolean ok = shiftDao.addRoom(newRoom);
            if (ok) {
                auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                        "THÊM PHÒNG KHÁM", "Admin thêm phòng: " + code + " - " + name);
                Toast.makeText(AdminManageRoomsActivity.this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                loadRooms();
            } else {
                Toast.makeText(AdminManageRoomsActivity.this, "Thêm phòng thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
