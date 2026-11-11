package com.phongkham.app.activities.admin;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.phongkham.app.R;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.database.DatabaseHelper;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.SessionManager;

public class AdminConfigSlotsActivity extends AppCompatActivity {
    private ImageView ivBack;
    private EditText etMaxPatientsPerSlot, etMinHoursCancel, etClinicName;
    private Button btnSaveConfig;

    private DatabaseHelper dbHelper;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_config_slots);

        dbHelper = DatabaseHelper.getInstance(this);
        auditLogDao = new AuditLogDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadConfigs();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etMaxPatientsPerSlot = findViewById(R.id.etMaxPatientsPerSlot);
        etMinHoursCancel = findViewById(R.id.etMinHoursCancel);
        etClinicName = findViewById(R.id.etClinicName);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);
    }

    private void loadConfigs() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT value FROM " + DatabaseHelper.TABLE_CONFIGS + " WHERE key = 'MAX_PATIENTS_PER_SLOT'", null);
        if (c1.moveToFirst()) etMaxPatientsPerSlot.setText(c1.getString(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT value FROM " + DatabaseHelper.TABLE_CONFIGS + " WHERE key = 'MIN_HOURS_BEFORE_CANCEL'", null);
        if (c2.moveToFirst()) etMinHoursCancel.setText(c2.getString(0));
        c2.close();

        Cursor c3 = db.rawQuery("SELECT value FROM " + DatabaseHelper.TABLE_CONFIGS + " WHERE key = 'CLINIC_NAME'", null);
        if (c3.moveToFirst()) etClinicName.setText(c3.getString(0));
        c3.close();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSaveConfig.setOnClickListener(v -> {
            String maxPatients = etMaxPatientsPerSlot.getText().toString().trim();
            String minHours = etMinHoursCancel.getText().toString().trim();
            String name = etClinicName.getText().toString().trim();

            if (maxPatients.isEmpty() || minHours.isEmpty()) {
                Toast.makeText(this, "Vui lòng không để trống các trường cấu hình!", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            saveSingleConfig(db, "MAX_PATIENTS_PER_SLOT", maxPatients);
            saveSingleConfig(db, "MIN_HOURS_BEFORE_CANCEL", minHours);
            saveSingleConfig(db, "CLINIC_NAME", name);

            auditLogDao.log(sessionManager.getUserId(), sessionManager.getFullName(), User.ROLE_ADMIN,
                    "CẤU HÌNH HỆ THỐNG", "Admin cập nhật: Max bệnh nhân/slot = " + maxPatients + ", Giờ hủy tối thiểu = " + minHours + "h");

            Toast.makeText(this, "Đã lưu cấu hình quy định thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveSingleConfig(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict(DatabaseHelper.TABLE_CONFIGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
