package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.Invoice;
import com.phongkham.app.models.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InvoiceDao {
    private final DatabaseHelper dbHelper;

    public InvoiceDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean createOrUpdateInvoice(Invoice invoice) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();
        values.put("appointment_id", invoice.getAppointmentId());
        values.put("appointment_code", invoice.getAppointmentCode());
        values.put("patient_id", invoice.getPatientId());
        values.put("patient_name", invoice.getPatientName());
        values.put("consultation_fee", invoice.getConsultationFee());
        values.put("medicine_fee", invoice.getMedicineFee());
        values.put("test_fee", invoice.getTestFee());
        values.put("total_amount", invoice.getTotalAmount());
        values.put("status", invoice.getStatus());
        values.put("payment_method", invoice.getPaymentMethod());

        Cursor cursor = db.rawQuery("SELECT id FROM " + DatabaseHelper.TABLE_INVOICES + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(invoice.getAppointmentId())});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            int rows = db.update(DatabaseHelper.TABLE_INVOICES, values, "appointment_id = ?", 
                    new String[]{String.valueOf(invoice.getAppointmentId())});
            return rows > 0;
        } else {
            values.put("created_at", now);
            long id = db.insert(DatabaseHelper.TABLE_INVOICES, null, values);
            if (id != -1) {
                invoice.setId((int) id);
                return true;
            }
            return false;
        }
    }

    public Invoice getInvoiceByAppointmentId(int appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_INVOICES + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(appointmentId)});
        Invoice inv = null;
        if (cursor.moveToFirst()) {
            inv = new Invoice();
            inv.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            inv.setAppointmentId(cursor.getInt(cursor.getColumnIndexOrThrow("appointment_id")));
            inv.setAppointmentCode(cursor.getString(cursor.getColumnIndexOrThrow("appointment_code")));
            inv.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
            inv.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
            inv.setConsultationFee(cursor.getDouble(cursor.getColumnIndexOrThrow("consultation_fee")));
            inv.setMedicineFee(cursor.getDouble(cursor.getColumnIndexOrThrow("medicine_fee")));
            inv.setTestFee(cursor.getDouble(cursor.getColumnIndexOrThrow("test_fee")));
            inv.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
            inv.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            inv.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
            inv.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        }
        cursor.close();
        return inv;
    }

    public double getTotalRevenue() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(total_amount) FROM " + DatabaseHelper.TABLE_INVOICES + " WHERE status = 'PAID'", null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public boolean addReview(Review review) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();
        values.put("appointment_id", review.getAppointmentId());
        values.put("patient_id", review.getPatientId());
        values.put("patient_name", review.getPatientName());
        values.put("doctor_id", review.getDoctorId());
        values.put("rating", review.getRating());
        values.put("comment", review.getComment());
        values.put("created_at", now);

        long id = db.insert(DatabaseHelper.TABLE_REVIEWS, null, values);
        return id != -1;
    }

    public Review getReviewByAppointmentId(int appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_REVIEWS + " WHERE appointment_id = ?", 
                new String[]{String.valueOf(appointmentId)});
        Review r = null;
        if (cursor.moveToFirst()) {
            r = new Review();
            r.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            r.setAppointmentId(cursor.getInt(cursor.getColumnIndexOrThrow("appointment_id")));
            r.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
            r.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
            r.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));
            r.setRating((float) cursor.getDouble(cursor.getColumnIndexOrThrow("rating")));
            r.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
            r.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        }
        cursor.close();
        return r;
    }
}
