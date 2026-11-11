package com.phongkham.app.models;

import java.io.Serializable;

public class ClinicRoom implements Serializable {
    private String maPhong;      // e.g. "P101"
    private String tenPhong;     // e.g. "Phòng Khám Nội 1"
    private String loaiPhong;    // e.g. "Khám Nội", "Khám Ngoại", "Nhi Khoa", "Xét nghiệm"
    private String trangThai;    // "Đang hoạt động", "Bảo trì", "Tạm ngưng"

    public ClinicRoom() {
        this.trangThai = "Đang hoạt động";
    }

    public ClinicRoom(String maPhong, String tenPhong, String loaiPhong, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.loaiPhong = loaiPhong;
        this.trangThai = trangThai;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { this.loaiPhong = loaiPhong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
