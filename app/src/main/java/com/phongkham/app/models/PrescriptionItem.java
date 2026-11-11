package com.phongkham.app.models;

import java.io.Serializable;

public class PrescriptionItem implements Serializable {
    private String medicineName;
    private int quantity;
    private String unit; // viên, gói, chai...
    private String dosage; // 1 viên x 2 lần/ngày
    private String instructions; // Uống sau ăn

    public PrescriptionItem() {}

    public PrescriptionItem(String medicineName, int quantity, String unit, String dosage, String instructions) {
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unit = unit;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
