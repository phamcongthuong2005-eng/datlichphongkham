package com.phongkham.app.models;

import java.io.Serializable;

public class Appointment implements Serializable {
    public static final String STATUS_PENDING = "PENDING";         // Chưa xác nhận
    public static final String STATUS_CONFIRMED = "CONFIRMED";     // Đã xác nhận / Đã đến
    public static final String STATUS_WAITING = "WAITING";         // Đang chờ trong hàng đợi
    public static final String STATUS_CALLED = "CALLED";           // Đã gọi
    public static final String STATUS_EXAMINING = "EXAMINING";     // Đang khám
    public static final String STATUS_COMPLETED = "COMPLETED";     // Đã khám xong
    public static final String STATUS_CANCELLED = "CANCELLED";     // Đã hủy
    public static final String STATUS_NOSHOW = "NOSHOW";           // Vắng mặt

    private int id;
    private String appointmentCode;
    private int patientId;
    private String patientName;
    private String patientPhone;
    private int doctorId;
    private String doctorName;
    private String specialtyName;
    private String appointmentDate; // yyyy-MM-dd
    private String timeSlot;        // e.g. "08:00 - 08:30"
    private String roomCode;        // e.g. "P101"
    private String symptoms;
    private String status;
    private String appointmentType; // "Khám theo hẹn", "Khám không hẹn", "Tái khám"
    private int queueNumber;
    private int callCount;          // Số lần gọi lại
    private String arrivalTime;     // Thời gian đến
    private String cancelReason;    // Lý do hủy
    private double fee;
    private boolean isPaid;
    private String createdAt;

    public Appointment() {
        this.status = STATUS_PENDING;
        this.appointmentType = "Khám theo hẹn";
        this.queueNumber = 0;
        this.callCount = 0;
        this.roomCode = "P101";
        this.isPaid = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAppointmentCode() { return appointmentCode; }
    public void setAppointmentCode(String appointmentCode) { this.appointmentCode = appointmentCode; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getRoomCode() { return roomCode != null ? roomCode : "P101"; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAppointmentType() { return appointmentType != null ? appointmentType : "Khám theo hẹn"; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public int getQueueNumber() { return queueNumber; }
    public void setQueueNumber(int queueNumber) { this.queueNumber = queueNumber; }

    public int getCallCount() { return callCount; }
    public void setCallCount(int callCount) { this.callCount = callCount; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
