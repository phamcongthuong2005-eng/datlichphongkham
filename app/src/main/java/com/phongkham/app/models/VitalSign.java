package com.phongkham.app.models;

import java.io.Serializable;

public class VitalSign implements Serializable {
    private int id;
    private int appointmentId;
    private int patientId;
    private double height; // cm
    private double weight; // kg
    private double temperature; // °C
    private String bloodPressure; // mmHg (e.g. "120/80")
    private int heartRate; // bpm
    private int spo2; // %
    private String otherNotes;
    private String warningAlert; // Cảnh báo bất thường nếu có
    private String measuredBy;
    private String measuredAt;

    public VitalSign() {}

    public VitalSign(int appointmentId, int patientId, double height, double weight, 
                     double temperature, String bloodPressure, int heartRate, int spo2, 
                     String measuredBy, String measuredAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.height = height;
        this.weight = weight;
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.measuredBy = measuredBy;
        this.measuredAt = measuredAt;
        checkWarnings();
    }

    public void checkWarnings() {
        StringBuilder sb = new StringBuilder();
        if (temperature >= 38.0) sb.append("• Sốt cao (≥ 38°C)\n");
        if (spo2 > 0 && spo2 < 95) sb.append("• SpO₂ thấp nguy hiểm (< 95%)\n");
        if (heartRate > 100) sb.append("• Nhịp tim nhanh (> 100 bpm)\n");
        else if (heartRate > 0 && heartRate < 60) sb.append("• Nhịp tim chậm (< 60 bpm)\n");
        this.warningAlert = sb.toString().trim();
    }

    public double calculateBmi() {
        if (height > 0 && weight > 0) {
            double heightInMeters = height / 100.0;
            return Math.round((weight / (heightInMeters * heightInMeters)) * 10.0) / 10.0;
        }
        return 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { 
        this.temperature = temperature; 
        checkWarnings();
    }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { 
        this.heartRate = heartRate; 
        checkWarnings();
    }

    public int getSpo2() { return spo2; }
    public void setSpo2(int spo2) { 
        this.spo2 = spo2; 
        checkWarnings();
    }

    public String getOtherNotes() { return otherNotes; }
    public void setOtherNotes(String otherNotes) { this.otherNotes = otherNotes; }

    public String getWarningAlert() { return warningAlert; }
    public void setWarningAlert(String warningAlert) { this.warningAlert = warningAlert; }

    public String getMeasuredBy() { return measuredBy; }
    public void setMeasuredBy(String measuredBy) { this.measuredBy = measuredBy; }

    public String getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(String measuredAt) { this.measuredAt = measuredAt; }
}
