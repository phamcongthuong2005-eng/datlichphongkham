package com.phongkham.app.models;

import java.io.Serializable;

public class Invoice implements Serializable {
    public static final String STATUS_UNPAID = "UNPAID";
    public static final String STATUS_PAID = "PAID";

    private int id;
    private int appointmentId;
    private String appointmentCode;
    private int patientId;
    private String patientName;
    private double consultationFee;
    private double medicineFee;
    private double testFee;
    private double totalAmount;
    private String status;
    private String paymentMethod; // TIỀN MẶT, CHUYỂN KHOẢN, BHYT
    private String createdAt;

    public Invoice() {
        this.status = STATUS_UNPAID;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getAppointmentCode() { return appointmentCode; }
    public void setAppointmentCode(String appointmentCode) { this.appointmentCode = appointmentCode; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public double getMedicineFee() { return medicineFee; }
    public void setMedicineFee(double medicineFee) { this.medicineFee = medicineFee; }

    public double getTestFee() { return testFee; }
    public void setTestFee(double testFee) { this.testFee = testFee; }

    public double getTotalAmount() { return consultationFee + medicineFee + testFee; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
