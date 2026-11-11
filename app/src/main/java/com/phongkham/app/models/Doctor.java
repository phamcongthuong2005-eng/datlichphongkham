package com.phongkham.app.models;

import java.io.Serializable;

public class Doctor implements Serializable {
    private int id;
    private int userId;
    private String fullName;
    private int specialtyId;
    private String specialtyName;
    private String degree;
    private int experienceYears;
    private double consultationFee;
    private float rating;
    private String bio;
    private String workingDays;
    private boolean isAvailable;

    public Doctor() {
        this.isAvailable = true;
    }

    public Doctor(int id, int userId, String fullName, int specialtyId, String specialtyName, 
                  String degree, int experienceYears, double consultationFee, float rating, String bio) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.degree = degree;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.rating = rating;
        this.bio = bio;
        this.workingDays = "Thứ 2 - Thứ 7";
        this.isAvailable = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(int specialtyId) { this.specialtyId = specialtyId; }

    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
