package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AppointmentDao {
    private final DatabaseHelper dbHelper;
    private final DoctorDao doctorDao;

    public AppointmentDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.doctorDao = new DoctorDao(context);
    }

    public boolean createAppointment(Appointment appt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("appointment_code", appt.getAppointmentCode());
        values.put("patient_id", appt.getPatientId());
        values.put("patient_name", appt.getPatientName());
        values.put("patient_phone", appt.getPatientPhone());
        values.put("doctor_id", appt.getDoctorId());
        values.put("doctor_name", appt.getDoctorName());
        values.put("specialty_name", appt.getSpecialtyName());
        values.put("appointment_date", appt.getAppointmentDate());
        values.put("time_slot", appt.getTimeSlot());
        values.put("symptoms", appt.getSymptoms());
        values.put("status", appt.getStatus());
        values.put("queue_number", appt.getQueueNumber());
        values.put("fee", appt.getFee());
        values.put("is_paid", appt.isPaid() ? 1 : 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put("created_at", sdf.format(Calendar.getInstance().getTime()));

        long id = db.insert(DatabaseHelper.TABLE_APPOINTMENTS, null, values);
        if (id != -1) {
            appt.setId((int) id);
            // Increment slot booking
            doctorDao.incrementSlotBooking(appt.getDoctorId(), appt.getAppointmentDate(), appt.getTimeSlot());
            return true;
        }
        return false;
    }

    public boolean updateStatus(int appointmentId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Appointment appt = getAppointmentById(appointmentId);

        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        int rows = db.update(DatabaseHelper.TABLE_APPOINTMENTS, values, "id = ?", new String[]{String.valueOf(appointmentId)});

        if (rows > 0 && appt != null) {
            // If cancelled, free up the slot
            if (Appointment.STATUS_CANCELLED.equals(newStatus)) {
                doctorDao.decrementSlotBooking(appt.getDoctorId(), appt.getAppointmentDate(), appt.getTimeSlot());
            }
        }
        return rows > 0;
    }

    public boolean assignQueueNumber(int appointmentId, int queueNumber) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("queue_number", queueNumber);
        values.put("status", Appointment.STATUS_CONFIRMED);
        int rows = db.update(DatabaseHelper.TABLE_APPOINTMENTS, values, "id = ?", new String[]{String.valueOf(appointmentId)});
        return rows > 0;
    }

    public boolean updateDateTime(int appointmentId, String newDate, String newTimeSlot) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Appointment old = getAppointmentById(appointmentId);
        if (old == null) return false;

        // Decrement old slot
        doctorDao.decrementSlotBooking(old.getDoctorId(), old.getAppointmentDate(), old.getTimeSlot());

        ContentValues values = new ContentValues();
        values.put("appointment_date", newDate);
        values.put("time_slot", newTimeSlot);
        int rows = db.update(DatabaseHelper.TABLE_APPOINTMENTS, values, "id = ?", new String[]{String.valueOf(appointmentId)});

        if (rows > 0) {
            // Increment new slot
            doctorDao.incrementSlotBooking(old.getDoctorId(), newDate, newTimeSlot);
            return true;
        }
        return false;
    }

    public Appointment getAppointmentById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_APPOINTMENTS + " WHERE id = ?", new String[]{String.valueOf(id)});
        Appointment appt = null;
        if (cursor.moveToFirst()) {
            appt = cursorToAppointment(cursor);
        }
        cursor.close();
        return appt;
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_APPOINTMENTS + 
                " WHERE patient_id = ? ORDER BY appointment_date DESC, time_slot DESC", 
                new String[]{String.valueOf(patientId)});
        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, String date) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_APPOINTMENTS + 
                " WHERE doctor_id = ? AND appointment_date = ? ORDER BY time_slot ASC", 
                new String[]{String.valueOf(doctorId), date});
        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public List<Appointment> getAppointmentsByDate(String date) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_APPOINTMENTS + 
                " WHERE appointment_date = ? ORDER BY queue_number ASC, time_slot ASC", 
                new String[]{date});
        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_APPOINTMENTS + " ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            list.add(cursorToAppointment(cursor));
        }
        cursor.close();
        return list;
    }

    public int getNextQueueNumber(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(queue_number) FROM " + DatabaseHelper.TABLE_APPOINTMENTS + " WHERE appointment_date = ?", new String[]{date});
        int max = 0;
        if (cursor.moveToFirst()) {
            max = cursor.getInt(0);
        }
        cursor.close();
        return max + 1;
    }

    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment a = new Appointment();
        a.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        a.setAppointmentCode(cursor.getString(cursor.getColumnIndexOrThrow("appointment_code")));
        a.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
        a.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
        a.setPatientPhone(cursor.getString(cursor.getColumnIndexOrThrow("patient_phone")));
        a.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));
        a.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
        a.setSpecialtyName(cursor.getString(cursor.getColumnIndexOrThrow("specialty_name")));
        a.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow("appointment_date")));
        a.setTimeSlot(cursor.getString(cursor.getColumnIndexOrThrow("time_slot")));
        a.setSymptoms(cursor.getString(cursor.getColumnIndexOrThrow("symptoms")));
        a.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        a.setQueueNumber(cursor.getInt(cursor.getColumnIndexOrThrow("queue_number")));
        a.setFee(cursor.getDouble(cursor.getColumnIndexOrThrow("fee")));
        a.setPaid(cursor.getInt(cursor.getColumnIndexOrThrow("is_paid")) == 1);
        a.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return a;
    }
}
