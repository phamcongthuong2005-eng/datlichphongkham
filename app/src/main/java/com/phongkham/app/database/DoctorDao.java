package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.Specialty;
import com.phongkham.app.models.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class DoctorDao {
    private final DatabaseHelper dbHelper;

    public DoctorDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<Specialty> getAllSpecialties() {
        List<Specialty> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_SPECIALTIES, null);
        while (cursor.moveToNext()) {
            Specialty s = new Specialty();
            s.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            s.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            s.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            s.setIconName(cursor.getString(cursor.getColumnIndexOrThrow("icon_name")));
            list.add(s);
        }
        cursor.close();
        return list;
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_DOCTORS, null);
        while (cursor.moveToNext()) {
            list.add(cursorToDoctor(cursor));
        }
        cursor.close();
        return list;
    }

    public Doctor getDoctorById(int doctorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_DOCTORS + " WHERE id = ?", new String[]{String.valueOf(doctorId)});
        Doctor doc = null;
        if (cursor.moveToFirst()) {
            doc = cursorToDoctor(cursor);
        }
        cursor.close();
        return doc;
    }

    public Doctor getDoctorByUserId(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_DOCTORS + " WHERE user_id = ?", new String[]{String.valueOf(userId)});
        Doctor doc = null;
        if (cursor.moveToFirst()) {
            doc = cursorToDoctor(cursor);
        }
        cursor.close();
        return doc;
    }

    public List<Doctor> searchDoctors(String keyword, Integer specialtyId) {
        List<Doctor> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        String[] args;

        if (specialtyId != null && specialtyId > 0) {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_DOCTORS + " WHERE specialty_id = ? AND full_name LIKE ?";
            args = new String[]{String.valueOf(specialtyId), "%" + keyword + "%"};
        } else {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_DOCTORS + " WHERE full_name LIKE ? OR specialty_name LIKE ?";
            args = new String[]{"%" + keyword + "%", "%" + keyword + "%"};
        }

        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            list.add(cursorToDoctor(cursor));
        }
        cursor.close();
        return list;
    }

    public boolean addDoctor(Doctor doctor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", doctor.getUserId());
        values.put("full_name", doctor.getFullName());
        values.put("specialty_id", doctor.getSpecialtyId());
        values.put("specialty_name", doctor.getSpecialtyName());
        values.put("degree", doctor.getDegree());
        values.put("experience_years", doctor.getExperienceYears());
        values.put("consultation_fee", doctor.getConsultationFee());
        values.put("rating", doctor.getRating());
        values.put("bio", doctor.getBio());
        values.put("working_days", doctor.getWorkingDays());
        values.put("is_available", doctor.isAvailable() ? 1 : 0);

        long id = db.insert(DatabaseHelper.TABLE_DOCTORS, null, values);
        return id != -1;
    }

    public boolean updateDoctor(Doctor doctor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", doctor.getFullName());
        values.put("specialty_id", doctor.getSpecialtyId());
        values.put("specialty_name", doctor.getSpecialtyName());
        values.put("degree", doctor.getDegree());
        values.put("experience_years", doctor.getExperienceYears());
        values.put("consultation_fee", doctor.getConsultationFee());
        values.put("bio", doctor.getBio());
        values.put("working_days", doctor.getWorkingDays());
        values.put("is_available", doctor.isAvailable() ? 1 : 0);

        int rows = db.update(DatabaseHelper.TABLE_DOCTORS, values, "id = ?", new String[]{String.valueOf(doctor.getId())});
        return rows > 0;
    }

    public boolean deleteDoctor(int doctorId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_DOCTORS, "id = ?", new String[]{String.valueOf(doctorId)});
        return rows > 0;
    }

    public List<TimeSlot> getTimeSlots(int doctorId, String date) {
        List<TimeSlot> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_TIME_SLOTS + 
                " WHERE doctor_id = ? AND slot_date = ? ORDER BY slot_time ASC", 
                new String[]{String.valueOf(doctorId), date});
        while (cursor.moveToNext()) {
            TimeSlot slot = new TimeSlot();
            slot.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            slot.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));
            slot.setSlotDate(cursor.getString(cursor.getColumnIndexOrThrow("slot_date")));
            slot.setSlotTime(cursor.getString(cursor.getColumnIndexOrThrow("slot_time")));
            slot.setMaxPatients(cursor.getInt(cursor.getColumnIndexOrThrow("max_patients")));
            slot.setBookedPatients(cursor.getInt(cursor.getColumnIndexOrThrow("booked_patients")));
            slot.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow("is_available")) == 1);
            list.add(slot);
        }
        cursor.close();
        return list;
    }

    public boolean incrementSlotBooking(int doctorId, String date, String timeSlot) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_TIME_SLOTS + 
                " SET booked_patients = booked_patients + 1 WHERE doctor_id = ? AND slot_date = ? AND slot_time = ?", 
                new Object[]{doctorId, date, timeSlot});
        return true;
    }

    public boolean decrementSlotBooking(int doctorId, String date, String timeSlot) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_TIME_SLOTS + 
                " SET booked_patients = MAX(0, booked_patients - 1) WHERE doctor_id = ? AND slot_date = ? AND slot_time = ?", 
                new Object[]{doctorId, date, timeSlot});
        return true;
    }

    private Doctor cursorToDoctor(Cursor cursor) {
        Doctor d = new Doctor();
        d.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        d.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        d.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        d.setSpecialtyId(cursor.getInt(cursor.getColumnIndexOrThrow("specialty_id")));
        d.setSpecialtyName(cursor.getString(cursor.getColumnIndexOrThrow("specialty_name")));
        d.setDegree(cursor.getString(cursor.getColumnIndexOrThrow("degree")));
        d.setExperienceYears(cursor.getInt(cursor.getColumnIndexOrThrow("experience_years")));
        d.setConsultationFee(cursor.getDouble(cursor.getColumnIndexOrThrow("consultation_fee")));
        d.setRating((float) cursor.getDouble(cursor.getColumnIndexOrThrow("rating")));
        d.setBio(cursor.getString(cursor.getColumnIndexOrThrow("bio")));
        d.setWorkingDays(cursor.getString(cursor.getColumnIndexOrThrow("working_days")));
        d.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow("is_available")) == 1);
        return d;
    }
}
