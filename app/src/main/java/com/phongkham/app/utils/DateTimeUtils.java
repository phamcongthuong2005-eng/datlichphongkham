package com.phongkham.app.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static String getToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("###,###,### VNĐ");
        return formatter.format(amount);
    }

    public static String generateAppointmentCode() {
        int randomNum = (int) (Math.random() * 9000) + 1000;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        return "PK-" + year + "-" + randomNum;
    }

    public static String getStatusVietnamese(String status) {
        if (status == null) return "Chờ khám";
        switch (status) {
            case "PENDING": return "Chờ khám";
            case "CONFIRMED": return "Đã tiếp nhận";
            case "EXAMINING": return "Đang khám";
            case "COMPLETED": return "Đã khám xong";
            case "CANCELLED": return "Đã hủy";
            case "NOSHOW": return "Vắng mặt";
            default: return status;
        }
    }
}
