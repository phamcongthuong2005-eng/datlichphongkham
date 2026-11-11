package com.phongkham.app.models;

import java.io.Serializable;

public class ShiftAssignment implements Serializable {
    private int id;
    private String maCa;        // "CA_SANG", "CA_CHIEU"
    private int maBacSi;
    private String tenBacSi;
    private int maYTa;
    private String tenYTa;
    private String maPhong;
    private String tenPhong;
    private String ngayLam;     // "2026-09-24"
    private String gioBatDau;   // "07:30"
    private String gioKetThuc;  // "11:30"
    private String trangThai;   // "Đang diễn ra", "Đã lên lịch", "Đã kết thúc", "Nghỉ phép"

    public ShiftAssignment() {
        this.trangThai = "Đã lên lịch";
    }

    public ShiftAssignment(int id, String maCa, int maBacSi, String tenBacSi, int maYTa, String tenYTa, 
                           String maPhong, String tenPhong, String ngayLam, String gioBatDau, String gioKetThuc, String trangThai) {
        this.id = id;
        this.maCa = maCa;
        this.maBacSi = maBacSi;
        this.tenBacSi = tenBacSi;
        this.maYTa = maYTa;
        this.tenYTa = tenYTa;
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.ngayLam = ngayLam;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }

    public int getMaBacSi() { return maBacSi; }
    public void setMaBacSi(int maBacSi) { this.maBacSi = maBacSi; }

    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }

    public int getMaYTa() { return maYTa; }
    public void setMaYTa(int maYTa) { this.maYTa = maYTa; }

    public String getTenYTa() { return tenYTa; }
    public void setTenYTa(String tenYTa) { this.tenYTa = tenYTa; }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getNgayLam() { return ngayLam; }
    public void setNgayLam(String ngayLam) { this.ngayLam = ngayLam; }

    public String getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(String gioBatDau) { this.gioBatDau = gioBatDau; }

    public String getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(String gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
