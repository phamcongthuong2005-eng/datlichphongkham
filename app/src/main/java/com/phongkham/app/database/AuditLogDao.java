package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.AuditLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AuditLogDao {
    private final DatabaseHelper dbHelper;

    public AuditLogDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public void log(int userId, String userName, String userRole, String action, String details) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("user_name", userName);
        values.put("user_role", userRole);
        values.put("action", action);
        values.put("details", details);
        values.put("timestamp", now);

        db.insert(DatabaseHelper.TABLE_AUDIT_LOGS, null, values);
    }

    public List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_AUDIT_LOGS + " ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            AuditLog log = new AuditLog();
            log.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            log.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            log.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
            log.setUserRole(cursor.getString(cursor.getColumnIndexOrThrow("user_role")));
            log.setAction(cursor.getString(cursor.getColumnIndexOrThrow("action")));
            log.setDetails(cursor.getString(cursor.getColumnIndexOrThrow("details")));
            log.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
            list.add(log);
        }
        cursor.close();
        return list;
    }
}
