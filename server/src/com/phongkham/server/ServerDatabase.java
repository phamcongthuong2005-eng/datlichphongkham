package com.phongkham.server;

import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerDatabase {
    // In-memory + synchronized thread-safe DB on Server
    public static final List<JSONObject> users = new ArrayList<>();
    public static final List<JSONObject> specialties = new ArrayList<>();
    public static final List<JSONObject> doctors = new ArrayList<>();
    public static final List<JSONObject> appointments = new ArrayList<>();
    public static final List<JSONObject> medicalRecords = new ArrayList<>();
    public static final List<JSONObject> invoices = new ArrayList<>();
    public static final List<JSONObject> reviews = new ArrayList<>();
    public static final List<JSONObject> auditLogs = new ArrayList<>();

    public static synchronized void init() {
        if (!users.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(new Date());

        // Users Phòng Khám Tư Nhân Lê Lợi - Cơ Xương Khớp (4 vai trò: ADMIN, DOCTOR, STAFF, PATIENT)
        users.add(createUser(1, "Chủ Phòng Khám Lê Lợi", "admin@gmail.com", "0900000001", "123456", "ADMIN"));
        users.add(createUser(2, "BS. CKI Phan Ba Hai Bình", "bacsi@gmail.com", "0900000002", "123456", "DOCTOR"));
        users.add(createUser(3, "ThS. BS Trần Thị Mai", "bsmai@gmail.com", "0900000003", "123456", "DOCTOR"));
        users.add(createUser(4, "PGS. TS Lê Hoàng Quân", "bsquan@gmail.com", "0900000004", "123456", "DOCTOR"));
        users.add(createUser(5, "BS. CKII Nguyễn Văn Hùng", "bshung@gmail.com", "0900000008", "123456", "DOCTOR"));
        users.add(createUser(6, "ThS. BS Phạm Thu Trang", "bstrang@gmail.com", "0900000009", "123456", "DOCTOR"));
        users.add(createUser(7, "Lễ Tân Lê Thị Hương", "letan@gmail.com", "0900000005", "123456", "STAFF"));
        users.add(createUser(8, "Lễ Tân Trần Minh Quân", "letan2@gmail.com", "0900000007", "123456", "STAFF"));
        users.add(createUser(9, "Nguyễn Hữu Hậu", "benhnhan@gmail.com", "0912345678", "123456", "PATIENT"));

        // 7 Dịch vụ chuyên khoa Cơ Xương Khớp chuẩn
        specialties.add(createSpecialty(1, "Khám Tổng Quát Cơ Xương Khớp", "Khám tầm soát hệ vận động, đau nhức xương khớp và cột sống"));
        specialties.add(createSpecialty(2, "Khám & Trị Liệu Cột Sống - Thoát Vị Đĩa Đệm", "Chuyên sâu thoát vị đĩa đệm thắt lưng L4-L5, thoái hóa cột sống"));
        specialties.add(createSpecialty(3, "Khám & Điều Trị Đau Cổ Vai Gáy", "Điều trị đau cổ vai gáy cấp và mãn tính, hội chứng chèn ép rễ"));
        specialties.add(createSpecialty(4, "Khám & Điều Trị Thoái Hóa Khớp", "Điều trị thoái hóa khớp háng, khớp cổ chân, viêm bao gân"));
        specialties.add(createSpecialty(5, "Bệnh Lý Khớp Gối & Tiêm Acid Hyaluronic / Huyết Tương", "Điều trị tràn dịch gối, tiêm chất nhờn sinh học bôi trơn khớp và PRP"));
        specialties.add(createSpecialty(6, "Đo Mật Độ Xương & Điều Trị Loãng Xương", "Tầm soát loãng xương DEXA, phác đồ bổ sung khoáng chất và phòng gãy xương"));
        specialties.add(createSpecialty(7, "Tái Khám Cơ Xương Khớp & Vật Lý Trị Liệu", "Tái khám theo dõi phác đồ, vật lý trị liệu phục hồi chức năng"));

        // 5 Bác sĩ chuyên khoa Cơ Xương Khớp
        doctors.add(createDoctor(1, 2, "BS. CKI Phan Ba Hai Bình", 1, "Khám Tổng Quát Cơ Xương Khớp", "Chuyên Khoa I", 12, 200000, 4.9, "12 năm kinh nghiệm BV Chợ Rẫy, tận tâm và chu đáo", "Thứ 2 - Thứ 7 (08:00 - 20:00)"));
        doctors.add(createDoctor(2, 3, "ThS. BS Trần Thị Mai", 2, "Khám & Trị Liệu Cột Sống - Thoát Vị Đĩa Đệm", "Thạc Sĩ", 9, 350000, 4.9, "Chuyên gia điều trị bảo tồn thoát vị đĩa đệm và cột sống", "Thứ 2 - Thứ 6 (08:00 - 20:00)"));
        doctors.add(createDoctor(3, 4, "PGS. TS Lê Hoàng Quân", 5, "Bệnh Lý Khớp Gối & Tiêm Acid Hyaluronic / Huyết Tương", "PGS. Tiến Sĩ", 20, 400000, 5.0, "Chuyên gia đầu ngành cơ xương khớp & khớp gối kỹ thuật cao", "Thứ 3, 5, 7 (08:00 - 20:00)"));
        doctors.add(createDoctor(4, 5, "BS. CKII Nguyễn Văn Hùng", 3, "Khám & Điều Trị Đau Cổ Vai Gáy", "Chuyên Khoa II", 15, 250000, 4.8, "Chuyên gia giảm đau cơ xương khớp và đau mỏi cổ vai gáy", "Thứ 2, 4, 6, CN (08:00 - 20:00)"));
        doctors.add(createDoctor(5, 6, "ThS. BS Phạm Thu Trang", 6, "Đo Mật Độ Xương & Điều Trị Loãng Xương", "Thạc Sĩ", 8, 300000, 4.9, "Chuyên gia chẩn đoán loãng xương và phục hồi chức năng vận động", "Thứ 2 - Thứ 7 (08:00 - 20:00)"));

        // Sample Appointment
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFmt.format(new Date());

        JSONObject appt = new JSONObject();
        appt.put("id", 1);
        appt.put("appointmentCode", "PK-2026-8891");
        appt.put("patientId", 9);
        appt.put("patientName", "Nguyễn Hữu Hậu");
        appt.put("patientPhone", "0912345678");
        appt.put("doctorId", 1);
        appt.put("doctorName", "BS. CKI Phan Ba Hai Bình");
        appt.put("specialtyName", "Khám & Trị Liệu Cột Sống - Thoát Vị Đĩa Đệm");
        appt.put("appointmentDate", today);
        appt.put("timeSlot", "08:30 - 09:00");
        appt.put("symptoms", "Đau thắt lưng âm ỉ lan xuống chân trái, tê bì bắp chân khi ngồi lâu");
        appt.put("status", "CONFIRMED");
        appt.put("queueNumber", 1);
        appt.put("fee", 350000);
        appt.put("isPaid", true);
        appt.put("createdAt", now);
        appointments.add(appt);

        // Audit Log
        logAudit(1, "Hệ Thống", "SYSTEM", "KHỞI TẠO", "Server TCP/IP & Database phòng khám đã khởi động");
    }

    public static synchronized JSONObject login(String account, String password) {
        JSONObject res = new JSONObject();
        for (JSONObject u : users) {
            if ((u.optString("email").equalsIgnoreCase(account) || u.optString("phone").equals(account))
                    && u.optString("password").equals(password)) {
                if (u.optBoolean("isLocked", false)) {
                    res.put("status", "ERROR");
                    res.put("message", "Tài khoản của bạn đã bị khóa!");
                    return res;
                }
                res.put("status", "SUCCESS");
                res.put("token", "TOKEN_" + UUID.randomUUID().toString());
                res.put("user", u);
                logAudit(u.optInt("id"), u.optString("fullName"), u.optString("role"), "ĐĂNG NHẬP", "Đăng nhập TCP thành công");
                return res;
            }
        }
        res.put("status", "ERROR");
        res.put("message", "Tài khoản hoặc mật khẩu không chính xác!");
        return res;
    }

    public static synchronized JSONObject register(JSONObject data) {
        JSONObject res = new JSONObject();
        String phone = data.optString("phone");
        String email = data.optString("email");

        for (JSONObject u : users) {
            if (u.optString("phone").equals(phone) || (email.length() > 3 && u.optString("email").equalsIgnoreCase(email))) {
                res.put("status", "ERROR");
                res.put("message", "Số điện thoại hoặc Email đã được đăng ký!");
                return res;
            }
        }

        int newId = users.size() + 1;
        JSONObject newUser = createUser(newId, data.optString("fullName"), email, phone, data.optString("password"), "PATIENT");
        users.add(newUser);

        res.put("status", "SUCCESS");
        res.put("token", "TOKEN_" + UUID.randomUUID().toString());
        res.put("user", newUser);
        logAudit(newId, newUser.optString("fullName"), "PATIENT", "ĐĂNG KÝ", "Đăng ký tài khoản bệnh nhân");
        return res;
    }

    public static synchronized JSONObject getSpecialties() {
        JSONObject res = new JSONObject();
        res.put("status", "SUCCESS");
        res.put("data", new JSONArray(specialties));
        return res;
    }

    public static synchronized JSONObject getDoctors(String keyword, int specialtyId) {
        JSONObject res = new JSONObject();
        JSONArray arr = new JSONArray();
        String kw = keyword.toLowerCase().trim();

        for (JSONObject d : doctors) {
            boolean matchSpec = (specialtyId <= 0 || d.optInt("specialtyId") == specialtyId);
            boolean matchKw = kw.isEmpty() || d.optString("fullName").toLowerCase().contains(kw) || d.optString("specialtyName").toLowerCase().contains(kw);
            if (matchSpec && matchKw) {
                arr.put(d);
            }
        }
        res.put("status", "SUCCESS");
        res.put("data", arr);
        return res;
    }

    public static synchronized JSONObject getAvailableSlots(int doctorId, String date) {
        JSONObject res = new JSONObject();
        JSONArray arr = new JSONArray();
        String[] slots = {"08:00 - 08:30", "08:30 - 09:00", "09:00 - 09:30", "09:30 - 10:00", "10:00 - 10:30", "14:00 - 14:30", "14:30 - 15:00", "15:00 - 15:30"};

        for (int i = 0; i < slots.length; i++) {
            String slotTime = slots[i];
            int bookedCount = 0;
            for (JSONObject a : appointments) {
                if (a.optInt("doctorId") == doctorId && a.optString("appointmentDate").equals(date)
                        && a.optString("timeSlot").equals(slotTime) && !a.optString("status").equals("CANCELLED")) {
                    bookedCount++;
                }
            }

            JSONObject slot = new JSONObject();
            slot.put("id", i + 1);
            slot.put("doctorId", doctorId);
            slot.put("slotDate", date);
            slot.put("slotTime", slotTime);
            slot.put("maxPatients", 3);
            slot.put("bookedPatients", bookedCount);
            slot.put("isAvailable", bookedCount < 3);
            arr.put(slot);
        }

        res.put("status", "SUCCESS");
        res.put("data", arr);
        return res;
    }

    public static synchronized JSONObject bookAppointment(JSONObject data) {
        JSONObject res = new JSONObject();
        int patientId = data.optInt("patientId");
        int doctorId = data.optInt("doctorId");
        String date = data.optString("appointmentDate");
        String slotTime = data.optString("timeSlot");

        // Business Rule 1: Check duplicate patient
        for (JSONObject a : appointments) {
            if (a.optInt("patientId") == patientId && a.optString("appointmentDate").equals(date)
                    && a.optString("timeSlot").equals(slotTime) && !a.optString("status").equals("CANCELLED")) {
                res.put("status", "ERROR");
                res.put("message", "Bạn đã có lịch khám khác trong khung giờ này!");
                return res;
            }
        }

        // Business Rule 2: Check full slot (max 3)
        int booked = 0;
        for (JSONObject a : appointments) {
            if (a.optInt("doctorId") == doctorId && a.optString("appointmentDate").equals(date)
                    && a.optString("timeSlot").equals(slotTime) && !a.optString("status").equals("CANCELLED")) {
                booked++;
            }
        }
        if (booked >= 3) {
            res.put("status", "ERROR");
            res.put("message", "Khung giờ này vừa mới đầy, vui lòng chọn giờ khác!");
            return res;
        }

        int newId = appointments.size() + 1;
        String code = "PK-2026-" + ((int)(Math.random() * 9000) + 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        JSONObject appt = new JSONObject(data.toString());
        appt.put("id", newId);
        appt.put("appointmentCode", code);
        appt.put("status", "PENDING");
        appt.put("queueNumber", 0);
        appt.put("isPaid", false);
        appt.put("createdAt", sdf.format(new Date()));
        appointments.add(appt);

        logAudit(patientId, data.optString("patientName"), "PATIENT", "ĐẶT LỊCH TCP", "Tạo lịch khám thành công mã " + code);

        res.put("status", "SUCCESS");
        res.put("appointmentCode", code);
        res.put("appointment", appt);
        return res;
    }

    public static synchronized JSONObject getPatientAppointments(int patientId) {
        JSONObject res = new JSONObject();
        JSONArray arr = new JSONArray();
        for (JSONObject a : appointments) {
            if (a.optInt("patientId") == patientId) {
                arr.put(a);
            }
        }
        res.put("status", "SUCCESS");
        res.put("data", arr);
        return res;
    }

    public static synchronized JSONObject getDoctorAppointments(int doctorId, String date) {
        JSONObject res = new JSONObject();
        JSONArray arr = new JSONArray();
        for (JSONObject a : appointments) {
            if (a.optInt("doctorId") == doctorId && (date == null || date.isEmpty() || a.optString("appointmentDate").equals(date))) {
                arr.put(a);
            }
        }
        res.put("status", "SUCCESS");
        res.put("data", arr);
        return res;
    }

    public static synchronized JSONObject getAllAppointments(String date) {
        JSONObject res = new JSONObject();
        JSONArray arr = new JSONArray();
        for (JSONObject a : appointments) {
            if (date == null || date.isEmpty() || a.optString("appointmentDate").equals(date)) {
                arr.put(a);
            }
        }
        res.put("status", "SUCCESS");
        res.put("data", arr);
        return res;
    }

    public static synchronized JSONObject updateStatus(int appointmentId, String newStatus) {
        JSONObject res = new JSONObject();
        for (JSONObject a : appointments) {
            if (a.optInt("id") == appointmentId) {
                a.put("status", newStatus);
                res.put("status", "SUCCESS");
                return res;
            }
        }
        res.put("status", "ERROR");
        res.put("message", "Không tìm thấy lịch hẹn ID: " + appointmentId);
        return res;
    }

    public static synchronized JSONObject checkInPatient(int appointmentId, String date) {
        JSONObject res = new JSONObject();
        int maxQ = 0;
        for (JSONObject a : appointments) {
            if (a.optString("appointmentDate").equals(date)) {
                maxQ = Math.max(maxQ, a.optInt("queueNumber", 0));
            }
        }
        int nextQ = maxQ + 1;

        for (JSONObject a : appointments) {
            if (a.optInt("id") == appointmentId) {
                a.put("queueNumber", nextQ);
                a.put("status", "CONFIRMED");
                res.put("status", "SUCCESS");
                res.put("queueNumber", nextQ);
                return res;
            }
        }
        res.put("status", "ERROR");
        res.put("message", "Không tìm thấy lịch hẹn");
        return res;
    }

    public static synchronized JSONObject saveMedicalRecord(JSONObject data) {
        JSONObject res = new JSONObject();
        int apptId = data.optInt("appointmentId");
        for (JSONObject r : medicalRecords) {
            if (r.optInt("appointmentId") == apptId) {
                medicalRecords.remove(r);
                break;
            }
        }
        medicalRecords.add(data);
        updateStatus(apptId, "COMPLETED");
        res.put("status", "SUCCESS");
        return res;
    }

    public static synchronized JSONObject getMedicalRecord(int appointmentId) {
        JSONObject res = new JSONObject();
        for (JSONObject r : medicalRecords) {
            if (r.optInt("appointmentId") == appointmentId) {
                res.put("status", "SUCCESS");
                res.put("data", r);
                return res;
            }
        }
        res.put("status", "ERROR");
        res.put("message", "Chưa có hồ sơ khám");
        return res;
    }

    public static synchronized JSONObject createInvoice(JSONObject data) {
        JSONObject res = new JSONObject();
        invoices.add(data);
        res.put("status", "SUCCESS");
        return res;
    }

    public static synchronized JSONObject addReview(JSONObject data) {
        JSONObject res = new JSONObject();
        reviews.add(data);
        res.put("status", "SUCCESS");
        return res;
    }

    public static synchronized JSONObject getAuditLogs() {
        JSONObject res = new JSONObject();
        res.put("status", "SUCCESS");
        res.put("data", new JSONArray(auditLogs));
        return res;
    }

    public static synchronized void logAudit(int userId, String userName, String role, String action, String details) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        JSONObject log = new JSONObject();
        log.put("id", auditLogs.size() + 1);
        log.put("userId", userId);
        log.put("userName", userName);
        log.put("userRole", role);
        log.put("action", action);
        log.put("details", details);
        log.put("timestamp", sdf.format(new Date()));
        auditLogs.add(0, log);
    }

    private static JSONObject createUser(int id, String name, String email, String phone, String pass, String role) {
        JSONObject u = new JSONObject();
        u.put("id", id);
        u.put("fullName", name);
        u.put("email", email);
        u.put("phone", phone);
        u.put("password", pass);
        u.put("role", role);
        u.put("isLocked", false);
        return u;
    }

    private static JSONObject createSpecialty(int id, String name, String desc) {
        JSONObject s = new JSONObject();
        s.put("id", id);
        s.put("name", name);
        s.put("description", desc);
        return s;
    }

    private static JSONObject createDoctor(int id, int userId, String name, int specId, String specName, String deg, int exp, double fee, double rating, String bio, String days) {
        JSONObject d = new JSONObject();
        d.put("id", id);
        d.put("userId", userId);
        d.put("fullName", name);
        d.put("specialtyId", specId);
        d.put("specialtyName", specName);
        d.put("degree", deg);
        d.put("experienceYears", exp);
        d.put("consultationFee", fee);
        d.put("rating", rating);
        d.put("bio", bio);
        d.put("workingDays", days);
        return d;
    }
}
