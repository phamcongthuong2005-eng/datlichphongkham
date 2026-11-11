package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.VitalSign;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class VitalSignDao {
    private final DatabaseHelper dbHelper;

    public VitalSignDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean saveVitalSign(VitalSign vs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        vs.checkWarnings();

        ContentValues values = new ContentValues();
        values.put("appointment_id", vs.getAppointmentId());
        values.put("patient_id", vs.getPatientId());
        values.put("height", vs.getHeight());
        values.put("weight", vs.getWeight());
        values.put("temperature", vs.getTemperature());
        values.put("blood_pressure", vs.getBloodPressure());
        values.put("heart_rate", vs.getHeartRate());
        values.put("spo2", vs.getSpo2());
        values.put("other_notes", vs.getOtherNotes());
        values.put("warning_alert", vs.getWarningAlert());
        values.put("measured_by", vs.getMeasuredBy());
        values.put("measured_at", now);

        Cursor cursor = db.rawQuery("SELECT id FROM " + DatabaseHelper.TABLE_VITAL_SIGNS + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(vs.getAppointmentId())});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            int rows = db.update(DatabaseHelper.TABLE_VITAL_SIGNS, values, "appointment_id = ?", 
                    new String[]{String.valueOf(vs.getAppointmentId())});
            return rows > 0;
        } else {
            long id = db.insert(DatabaseHelper.TABLE_VITAL_SIGNS, null, values);
            if (id != -1) {
                vs.setId((int) id);
                return true;
            }
            return false;
        }
    }

    public VitalSign getVitalSignByAppointmentId(int appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_VITAL_SIGNS + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(appointmentId)});
        VitalSign vs = null;
        if (cursor.moveToFirst()) {
            vs = new VitalSign();
            vs.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            vs.setAppointmentId(cursor.getInt(cursor.getColumnIndexOrThrow("appointment_id")));
            vs.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
            vs.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow("height")));
            vs.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow("weight")));
            vs.setTemperature(cursor.getDouble(cursor.getColumnIndexOrThrow("temperature")));
            vs.setBloodPressure(cursor.getString(cursor.getColumnIndexOrThrow("blood_pressure")));
            vs.setHeartRate(cursor.getInt(cursor.getColumnIndexOrThrow("heart_rate")));
            vs.setSpo2(cursor.getInt(cursor.getColumnIndexOrThrow("spo2")));
            vs.setOtherNotes(cursor.getString(cursor.getColumnIndexOrThrow("other_notes")));
            vs.setWarningAlert(cursor.getString(cursor.getColumnIndexOrThrow("warning_alert")));
            vs.setMeasuredBy(cursor.getString(cursor.getColumnIndexOrThrow("measured_by")));
            vs.setMeasuredAt(cursor.getString(cursor.getColumnIndexOrThrow("measured_at")));
        }
        cursor.close();
        return vs;
    }
}
