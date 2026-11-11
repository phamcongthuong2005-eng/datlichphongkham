# HỆ THỐNG ĐẶT LỊCH KHÁM & QUẢN LÝ PHÒNG KHÁM (JAVA - ANDROID STUDIO)

Dự án hoàn chỉnh được phát triển **100% bằng ngôn ngữ Java** theo tiêu chuẩn **Android Studio Native**, đáp ứng đầy đủ yêu cầu nghiệp vụ y tế phân quyền 4 vai trò: **Bệnh nhân (Patient)**, **Bác sĩ (Doctor)**, **Nhân viên lễ tân (Staff)** và **Quản trị viên (Admin)**.

---

## 🚀 1. Hướng Dẫn Mở & Chạy Trên Android Studio

1. Mở phần mềm **Android Studio**.
2. Chọn **Open** (hoặc **File -> Open**) -> Trỏ tới thư mục: `d:\laptrinhmang`.
3. Chờ Android Studio đồng bộ xong Gradle (**Gradle Sync Finished**).
4. Chọn thiết bị:
   - **Máy ảo (Android Emulator)**: Ví dụ Pixel 6 / Pixel 7 / Nexus (Android 8.0 trở lên).
   - **Điện thoại thật**: Cắm cáp USB và bật chế độ *USB Debugging*.
5. Bấm nút **Run (▶ Shift + F10)** để cài đặt và khởi chạy ứng dụng!

---

## 🔑 2. Danh Sách Tài Khoản Demo Để Kiểm Thử (Seed Data)

Hệ thống đã tích hợp sẵn các nút chọn nhanh tài khoản tại màn hình Đăng nhập:

| Vai trò | Email / SĐT | Mật khẩu | Mô tả quyền hạn |
| :--- | :--- | :--- | :--- |
| **🩺 Bệnh nhân** | `benhnhan@gmail.com` (hoặc `0912345678`) | `123456` | Tìm bác sĩ, đặt lịch 5 bước, xem hồ sơ bệnh án, đơn thuốc, đánh giá sau khám |
| **👨‍⚕️ Bác sĩ** | `bacsi@gmail.com` (hoặc `0900000002`) | `123456` | Xem ca khám trong ngày, chẩn đoán, kê đơn thuốc động, chỉ định xét nghiệm, lịch trực |
| **👩‍💼 Lễ tân** | `letan@gmail.com` (hoặc `0900000005`) | `123456` | Tiếp nhận bệnh nhân, cấp STT hàng đợi, đặt lịch hộ tại quầy/hotline, lập hóa đơn |
| **🛡️ Quản trị (Admin)** | `admin@gmail.com` (hoặc `0900000001`) | `123456` | Quản lý user/khóa tài khoản, thêm/sửa bác sĩ, cấu hình khung giờ/giờ hủy, báo cáo, audit log |

---

## 🌟 3. Chi Tiết Các Chức Năng Đã Triển Khai (100% Java)

### A. Phân hệ Bệnh nhân (Patient)
- **Tài khoản**: Đăng nhập, Đăng ký bằng SĐT/Email, Quên/Đổi mật khẩu.
- **Hồ sơ sức khỏe**: BHYT, nhóm máu, tiền sử bệnh án, dị ứng thuốc.
- **Tìm kiếm & Khám phá**: Tra cứu bác sĩ theo chuyên khoa, tên, đánh giá sao, giá khám.
- **Quy trình đặt lịch 5 bước**:
  1. Chọn chuyên khoa -> 2. Chọn bác sĩ -> 3. Chọn ngày & khung giờ còn trống -> 4. Nhập lý do khám -> 5. Xác nhận & cấp mã lịch hẹn (`PK-2026-XXXX`).
- **Quản lý lịch hẹn**: Xem lịch sắp tới, đổi hoặc hủy lịch theo quy định (kiểm tra trước giờ hẹn).
- **Hồ sơ & Đơn thuốc**: Xem chẩn đoán của bác sĩ, đơn thuốc chi tiết, chỉ định xét nghiệm, ngày hẹn tái khám.
- **Đánh giá**: Chấm điểm sao 1-5⭐ và gửi nhận xét dịch vụ sau khi khám xong.

### B. Phân hệ Bác sĩ (Doctor)
- **Lịch làm việc**: Xem lịch trực cá nhân, đăng ký ngày nghỉ phép / báo bận.
- **Danh sách bệnh nhân**: Xem danh sách bệnh nhân đã đặt hẹn theo ngày, lọc theo trạng thái.
- **Tiến trình khám bệnh**:
  - Cập nhật trạng thái: *Chờ khám -> Đang khám -> Đã khám / Báo vắng*.
  - Ghi nhận triệu chứng lâm sàng, chẩn đoán xác định.
  - Kê đơn thuốc động (tên thuốc, số lượng, đơn vị, liều dùng, hướng dẫn).
  - Chỉ định xét nghiệm, hẹn ngày tái khám.
- **Xem tiền sử bệnh & dị ứng**: Hiển thị cảnh báo dị ứng của bệnh nhân khi đang khám.

### C. Phân hệ Nhân viên lễ tân (Staff)
- **Tiếp nhận bệnh nhân**: Xác nhận bệnh nhân đã có mặt tại quầy và **Cấp số thứ tự (Queue Number)** vào phòng khám.
- **Đặt lịch hộ**: Hỗ trợ đặt lịch cho bệnh nhân gọi qua điện thoại hoặc đến trực tiếp tại quầy.
- **Thu phí & Lập hóa đơn**: Thu tiền khám, tiền thuốc, xét nghiệm; hỗ trợ nhiều hình thức thanh toán (Tiền mặt, QR chuyển khoản, BHYT) và in biên lai.
- **Điều phối**: Hỗ trợ đổi, hủy lịch khám cho bệnh nhân khi có yêu cầu.

### D. Phân hệ Quản trị viên (Admin)
- **Quản lý tài khoản**: Xem toàn bộ tài khoản 4 role, Khóa / Mở khóa tài khoản ngay lập tức.
- **Quản lý Bác sĩ & Chuyên khoa**: Thêm mới, cập nhật thông tin, gán chuyên khoa, đổi phí khám, xóa bác sĩ.
- **Cấu hình Quy tắc hệ thống**: Cấu hình số lượng bệnh nhân tối đa / slot, cấu hình thời gian tối thiểu được phép hủy lịch trước giờ hẹn.
- **Báo cáo & Thống kê**: Tổng lượt đặt, tỷ lệ đã khám / hủy / vắng mặt, tổng doanh thu viện phí.
- **Audit Logs (Nhật ký hoạt động)**: Ghi nhận mọi thao tác quan trọng (sửa kết quả khám, hủy lịch, thu phí, cập nhật cấu hình).

---

## 🔒 4. Bộ Quy Tắc Nghiệp Vụ Được Thực Thi (`BusinessRulesValidator.java`)
- ❌ **Chặn quá tải slot**: Không cho đặt khi slot đã đủ số lượng bệnh nhân tối đa đã cấu hình.
- ❌ **Chặn thời gian quá khứ**: Không cho đặt lịch vào ngày/giờ đã qua.
- ❌ **Chặn trùng lịch**: 1 bệnh nhân không được có 2 lịch trùng giờ; 1 bác sĩ không có 2 ca khám chồng chéo.
- ❌ **Quy định hủy lịch**: Chỉ cho phép hủy lịch trước giờ hẹn ít nhất $X$ tiếng (cấu hình trong Admin).
- 🔄 **Giải phóng slot tự động**: Khi hủy lịch, khung giờ lập tức được mở lại cho bệnh nhân khác đặt.
- 📝 **Nhật ký vết thao tác**: Bác sĩ sửa bệnh án hoặc nhân viên thao tác đều được lưu vết trong `audit_logs`.

---

## 📂 5. Cấu Trúc Mã Nguồn (Java Package)

```
com.phongkham.app/
├── models/             # User, Doctor, Specialty, TimeSlot, Appointment, MedicalRecord, Review, Invoice, AuditLog
├── database/           # DatabaseHelper (SQLite), UserDao, DoctorDao, AppointmentDao, MedicalRecordDao, InvoiceDao, AuditLogDao
├── business/           # BusinessRulesValidator (Kiểm tra toàn bộ nghiệp vụ)
├── adapters/           # SpecialtyAdapter, DoctorAdapter, TimeSlotAdapter, AppointmentAdapter, PrescriptionAdapter, UserAdapter, AuditLogAdapter
├── activities/
│   ├── SplashActivity, AuthActivity
│   ├── patient/        # PatientMainActivity, BookAppointmentActivity, DoctorDetailActivity, PatientAppointmentsActivity, MedicalRecordDetailActivity, ReviewDoctorActivity, PatientProfileActivity
│   ├── doctor/         # DoctorMainActivity, DoctorExamineActivity, DoctorScheduleActivity
│   ├── staff/          # StaffMainActivity, StaffCreateBookingActivity, StaffInvoiceActivity
│   └── admin/          # AdminMainActivity, AdminManageUsersActivity, AdminManageDoctorsActivity, AdminConfigSlotsActivity, AdminReportsActivity, AdminAuditLogsActivity
└── utils/              # DateTimeUtils, SessionManager
```
