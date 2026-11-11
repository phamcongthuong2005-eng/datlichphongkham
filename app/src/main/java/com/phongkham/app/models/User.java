package com.phongkham.app.models;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_STAFF = "STAFF";   // Lễ tân
    public static final String ROLE_NURSE = "NURSE";   // Y tá
    public static final String ROLE_ADMIN = "ADMIN";   // Quản lý / Chủ phòng khám

    private int id;
    private String userCode; // e.g. US001
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role;
    private String birthDate;
    private String gender;
    private String address;
    private String insuranceNumber;
    private String medicalHistory;
    private String allergies;
    private String avatar;
    private boolean isLocked;

    public User() {}

    public User(int id, String fullName, String email, String phone, String password, String role) {
        this.id = id;
        this.userCode = "ND" + String.format("%03d", id);
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.isLocked = false;
    }

    public int getId() { return id; }
    public void setId(int id) { 
        this.id = id; 
        if (this.userCode == null) this.userCode = "ND" + String.format("%03d", id);
    }

    public String getUserCode() { return userCode != null ? userCode : "ND" + String.format("%03d", id); }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
}
