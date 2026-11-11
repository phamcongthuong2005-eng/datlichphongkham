package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.ClinicRoom;
import com.phongkham.app.models.ShiftAssignment;

import java.util.ArrayList;
import java.util.List;

public class ShiftDao {
    private final DatabaseHelper dbHelper;

    public ShiftDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<ClinicRoom> getAllRooms() {
        List<ClinicRoom> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ROOMS, null);
        while (cursor.moveToNext()) {
            ClinicRoom room = new ClinicRoom();
            room.setMaPhong(cursor.getString(cursor.getColumnIndexOrThrow("ma_phong")));
            room.setTenPhong(cursor.getString(cursor.getColumnIndexOrThrow("ten_phong")));
            room.setLoaiPhong(cursor.getString(cursor.getColumnIndexOrThrow("loai_phong")));
            room.setTrangThai(cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")));
            list.add(room);
        }
        cursor.close();
        return list;
    }

    public boolean addRoom(ClinicRoom room) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_phong", room.getMaPhong());
        values.put("ten_phong", room.getTenPhong());
        values.put("loai_phong", room.getLoaiPhong());
        values.put("trang_thai", room.getTrangThai());
        long id = db.insertWithOnConflict(DatabaseHelper.TABLE_ROOMS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return id != -1;
    }

    public List<ShiftAssignment> getShiftsByDate(String date) {
        List<ShiftAssignment> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_SHIFTS + " WHERE ngay_lam = ? ORDER BY id ASC", 
                new String[]{date});
        while (cursor.moveToNext()) {
            ShiftAssignment s = new ShiftAssignment();
            s.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            s.setMaCa(cursor.getString(cursor.getColumnIndexOrThrow("ma_ca")));
            s.setMaBacSi(cursor.getInt(cursor.getColumnIndexOrThrow("ma_bac_si")));
            s.setTenBacSi(cursor.getString(cursor.getColumnIndexOrThrow("ten_bac_si")));
            s.setMaYTa(cursor.getInt(cursor.getColumnIndexOrThrow("ma_y_ta")));
            s.setTenYTa(cursor.getString(cursor.getColumnIndexOrThrow("ten_y_ta")));
            s.setMaPhong(cursor.getString(cursor.getColumnIndexOrThrow("ma_phong")));
            s.setTenPhong(cursor.getString(cursor.getColumnIndexOrThrow("ten_phong")));
            s.setNgayLam(cursor.getString(cursor.getColumnIndexOrThrow("ngay_lam")));
            s.setGioBatDau(cursor.getString(cursor.getColumnIndexOrThrow("gio_bat_dau")));
            s.setGioKetThuc(cursor.getString(cursor.getColumnIndexOrThrow("gio_ket_thuc")));
            s.setTrangThai(cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")));
            list.add(s);
        }
        cursor.close();
        return list;
    }

    public boolean addShift(ShiftAssignment shift) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_ca", shift.getMaCa());
        values.put("ma_bac_si", shift.getMaBacSi());
        values.put("ten_bac_si", shift.getTenBacSi());
        values.put("ma_y_ta", shift.getMaYTa());
        values.put("ten_y_ta", shift.getTenYTa());
        values.put("ma_phong", shift.getMaPhong());
        values.put("ten_phong", shift.getTenPhong());
        values.put("ngay_lam", shift.getNgayLam());
        values.put("gio_bat_dau", shift.getGioBatDau());
        values.put("gio_ket_thuc", shift.getGioKetThuc());
        values.put("trang_thai", shift.getTrangThai());

        long id = db.insert(DatabaseHelper.TABLE_SHIFTS, null, values);
        return id != -1;
    }

    public boolean deleteRoom(String maPhong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ROOMS, "ma_phong = ?", new String[]{maPhong}) > 0;
    }

    public boolean deleteShift(int shiftId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SHIFTS, "id = ?", new String[]{String.valueOf(shiftId)}) > 0;
    }
}
