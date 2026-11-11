package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.MedicalRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicalRecordDao {
    private final DatabaseHelper dbHelper;

    public MedicalRecordDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean saveRecord(MedicalRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();
        values.put("appointment_id", record.getAppointmentId());
        values.put("patient_id", record.getPatientId());
        values.put("doctor_id", record.getDoctorId());
        values.put("symptoms", record.getSymptoms());
        values.put("diagnosis", record.getDiagnosis());
        values.put("differential_diagnosis", record.getDifferentialDiagnosis());
        values.put("patient_medical_history", record.getPatientMedicalHistory());
        values.put("family_history", record.getFamilyHistory());
        values.put("notes", record.getNotes());
        values.put("tests_required", record.getTestsRequired());
        values.put("follow_up_date", record.getFollowUpDate());
        values.put("prescription_json", record.getPrescriptionJson());
        values.put("updated_at", now);

        // Check if exists
        Cursor cursor = db.rawQuery("SELECT id FROM " + DatabaseHelper.TABLE_MEDICAL_RECORDS + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(record.getAppointmentId())});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            int rows = db.update(DatabaseHelper.TABLE_MEDICAL_RECORDS, values, "appointment_id = ?", 
                    new String[]{String.valueOf(record.getAppointmentId())});
            return rows > 0;
        } else {
            values.put("created_at", now);
            long id = db.insert(DatabaseHelper.TABLE_MEDICAL_RECORDS, null, values);
            if (id != -1) {
                record.setId((int) id);
                return true;
            }
            return false;
        }
    }

    public MedicalRecord getRecordByAppointmentId(int appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_MEDICAL_RECORDS + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(appointmentId)});
        MedicalRecord record = null;
        if (cursor.moveToFirst()) {
            record = cursorToRecord(cursor);
        }
        cursor.close();
        return record;
    }

    public List<MedicalRecord> getRecordsByPatientId(int patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_MEDICAL_RECORDS + 
                " WHERE patient_id = ? ORDER BY id DESC", new String[]{String.valueOf(patientId)});
        while (cursor.moveToNext()) {
            list.add(cursorToRecord(cursor));
        }
        cursor.close();
        return list;
    }

    private MedicalRecord cursorToRecord(Cursor cursor) {
        MedicalRecord r = new MedicalRecord();
        r.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        r.setAppointmentId(cursor.getInt(cursor.getColumnIndexOrThrow("appointment_id")));
        r.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
        r.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));
        r.setSymptoms(cursor.getString(cursor.getColumnIndexOrThrow("symptoms")));
        r.setDiagnosis(cursor.getString(cursor.getColumnIndexOrThrow("diagnosis")));
        int diffIdx = cursor.getColumnIndex("differential_diagnosis");
        if (diffIdx != -1) r.setDifferentialDiagnosis(cursor.getString(diffIdx));
        r.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        r.setTestsRequired(cursor.getString(cursor.getColumnIndexOrThrow("tests_required")));
        r.setFollowUpDate(cursor.getString(cursor.getColumnIndexOrThrow("follow_up_date")));
        r.setPrescriptionJson(cursor.getString(cursor.getColumnIndexOrThrow("prescription_json")));
        r.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        r.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        return r;
    }
}
