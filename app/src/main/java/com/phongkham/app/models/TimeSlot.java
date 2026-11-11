package com.phongkham.app.models;

import java.io.Serializable;

public class TimeSlot implements Serializable {
    private int id;
    private int doctorId;
    private String slotDate; // yyyy-MM-dd
    private String slotTime; // e.g. "08:00 - 08:30"
    private int maxPatients;
    private int bookedPatients;
    private boolean isAvailable;

    public TimeSlot() {
        this.maxPatients = 3;
        this.bookedPatients = 0;
        this.isAvailable = true;
    }

    public TimeSlot(int id, int doctorId, String slotDate, String slotTime, int maxPatients, int bookedPatients) {
        this.id = id;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.maxPatients = maxPatients;
        this.bookedPatients = bookedPatients;
        this.isAvailable = bookedPatients < maxPatients;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getSlotDate() { return slotDate; }
    public void setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public String getSlotTime() { return slotTime; }
    public void setSlotTime(String slotTime) { this.slotTime = slotTime; }

    public int getMaxPatients() { return maxPatients; }
    public void setMaxPatients(int maxPatients) { this.maxPatients = maxPatients; }

    public int getBookedPatients() { return bookedPatients; }
    public void setBookedPatients(int bookedPatients) { this.bookedPatients = bookedPatients; }

    public boolean isAvailable() { return isAvailable && (bookedPatients < maxPatients); }
    public void setAvailable(boolean available) { isAvailable = available; }
}
