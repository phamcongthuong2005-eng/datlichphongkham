package com.phongkham.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecord implements Serializable {
    private int id;
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String symptoms;
    private String patientMedicalHistory;
    private String familyHistory;          // Tiền sử gia đình
    private String diagnosis;              // Chẩn đoán chính
    private String differentialDiagnosis;  // Chẩn đoán phân biệt
    private String notes;
    private String testsRequired;          // Chỉ định xét nghiệm
    private String testResults;            // Kết quả xét nghiệm
    private String followUpDate;           // Ngày hẹn tái khám
    private String followUpReason;         // Lý do tái khám
    private String referralSpecialty;      // Chuyên khoa chuyển viện/tái khám
    private VitalSign vitalSign;           // Chỉ số sinh hiệu do y tá nhập
    private String prescriptionJson;
    private List<PrescriptionItem> prescriptionItems;
    private String createdAt;
    private String updatedAt;

    public MedicalRecord() {
        this.prescriptionItems = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getPatientMedicalHistory() { return patientMedicalHistory; }
    public void setPatientMedicalHistory(String patientMedicalHistory) { this.patientMedicalHistory = patientMedicalHistory; }

    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getDifferentialDiagnosis() { return differentialDiagnosis; }
    public void setDifferentialDiagnosis(String differentialDiagnosis) { this.differentialDiagnosis = differentialDiagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTestsRequired() { return testsRequired; }
    public void setTestsRequired(String testsRequired) { this.testsRequired = testsRequired; }

    public String getTestResults() { return testResults; }
    public void setTestResults(String testResults) { this.testResults = testResults; }

    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

    public String getFollowUpReason() { return followUpReason; }
    public void setFollowUpReason(String followUpReason) { this.followUpReason = followUpReason; }

    public String getReferralSpecialty() { return referralSpecialty; }
    public void setReferralSpecialty(String referralSpecialty) { this.referralSpecialty = referralSpecialty; }

    public VitalSign getVitalSign() { return vitalSign; }
    public void setVitalSign(VitalSign vitalSign) { this.vitalSign = vitalSign; }

    public String getPrescriptionJson() { return prescriptionJson; }
    public void setPrescriptionJson(String prescriptionJson) { this.prescriptionJson = prescriptionJson; }

    public List<PrescriptionItem> getPrescriptionItems() { return prescriptionItems; }
    public void setPrescriptionItems(List<PrescriptionItem> prescriptionItems) { this.prescriptionItems = prescriptionItems; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
