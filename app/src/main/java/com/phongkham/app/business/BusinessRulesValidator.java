package com.phongkham.app.business;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.database.DatabaseHelper;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BusinessRulesValidator {
    private final DatabaseHelper dbHelper;

    public BusinessRulesValidator(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Rule 1: Không được đặt lịch trong quá khứ
     */
    public boolean isPastDateTime(String dateStr, String timeSlot) {
        try {
            // timeSlot format: "08:00 - 08:30"
            String startTime = timeSlot.split("-")[0].trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date targetDate = sdf.parse(dateStr + " " + startTime);
            Date now = new Date();
            return targetDate != null && targetDate.before(now);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rule 2: Mỗi khung giờ chỉ được nhận số bệnh nhân tối đa đã cấu hình (mặc định 3)
     */
    public boolean isSlotFull(int doctorId, String date, String timeSlot) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT max_patients, booked_patients FROM " + DatabaseHelper.TABLE_TIME_SLOTS + 
                " WHERE doctor_id = ? AND slot_date = ? AND slot_time = ?", 
                new String[]{String.valueOf(doctorId), date, timeSlot});
        boolean full = false;
        if (cursor.moveToFirst()) {
            int max = cursor.getInt(0);
            int booked = cursor.getInt(1);
            full = (booked >= max);
        }
        cursor.close();
        return full;
    }

    /**
     * Rule 3: Một bệnh nhân không nên có hai lịch trùng thời gian
     */
    public boolean hasPatientOverlap(int patientId, String date, String timeSlot, Integer excludeApptId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APPOINTMENTS + 
                " WHERE patient_id = ? AND appointment_date = ? AND time_slot = ? AND status != 'CANCELLED'";
        String[] args;
        if (excludeApptId != null) {
            query += " AND id != ?";
            args = new String[]{String.valueOf(patientId), date, timeSlot, String.valueOf(excludeApptId)};
        } else {
            args = new String[]{String.valueOf(patientId), date, timeSlot};
        }
        Cursor cursor = db.rawQuery(query, args);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count > 0;
    }

    /**
     * Rule 4: Một bác sĩ không được có hai ca khám trùng nhau (nếu cùng slot đã đầy)
     */
    public boolean hasDoctorOverlap(int doctorId, String date, String timeSlot) {
        return isSlotFull(doctorId, date, timeSlot);
    }

    /**
     * Rule 5: Chỉ được đổi hoặc hủy lịch trước giờ khám một khoảng thời gian quy định (mặc định 2 tiếng)
     */
    public boolean canCancelOrReschedule(String appointmentDate, String timeSlot) {
        try {
            int minHours = getMinHoursBeforeCancel();
            String startTime = timeSlot.split("-")[0].trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date targetDate = sdf.parse(appointmentDate + " " + startTime);
            if (targetDate == null) return false;

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, minHours);
            Date minAllowedDate = cal.getTime();

            // Nếu thời gian khám sau thời gian quy định tối thiểu thì được phép hủy
            return targetDate.after(minAllowedDate);
        } catch (Exception e) {
            return false;
        }
    }

    public int getMinHoursBeforeCancel() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT value FROM " + DatabaseHelper.TABLE_CONFIGS + " WHERE key = 'MIN_HOURS_BEFORE_CANCEL'", null);
        int hours = 2;
        if (cursor.moveToFirst()) {
            try {
                hours = Integer.parseInt(cursor.getString(0));
            } catch (Exception ignored) {}
        }
        cursor.close();
        return hours;
    }
}
