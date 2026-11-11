package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.models.Specialty;
import com.phongkham.app.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "phongkham_leloi.db";
    private static final int DATABASE_VERSION = 3;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SPECIALTIES = "specialties";
    public static final String TABLE_DOCTORS = "doctors";
    public static final String TABLE_ROOMS = "clinic_rooms";
    public static final String TABLE_SHIFTS = "shift_assignments";
    public static final String TABLE_TIME_SLOTS = "time_slots";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_VITAL_SIGNS = "vital_signs";
    public static final String TABLE_MEDICAL_RECORDS = "medical_records";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_INVOICES = "invoices";
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_AUDIT_LOGS = "audit_logs";
    public static final String TABLE_CONFIGS = "system_configs";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users table (Hỗ trợ 5 vai trò: PATIENT, DOCTOR, STAFF, NURSE, ADMIN)
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_code TEXT, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT UNIQUE, " +
                "phone TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "birth_date TEXT, " +
                "gender TEXT, " +
                "address TEXT, " +
                "insurance_number TEXT, " +
                "medical_history TEXT, " +
                "allergies TEXT, " +
                "avatar TEXT, " +
                "is_locked INTEGER DEFAULT 0)");

        // 2. Specialties table
        db.execSQL("CREATE TABLE " + TABLE_SPECIALTIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "specialty_code TEXT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "standard_fee REAL DEFAULT 200000, " +
                "icon_name TEXT)");

        // 3. Clinic Rooms table
        db.execSQL("CREATE TABLE " + TABLE_ROOMS + " (" +
                "ma_phong TEXT PRIMARY KEY, " +
                "ten_phong TEXT NOT NULL, " +
                "loai_phong TEXT, " +
                "trang_thai TEXT DEFAULT 'Đang hoạt động')");

        // 4. Doctors table
        db.execSQL("CREATE TABLE " + TABLE_DOCTORS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "full_name TEXT NOT NULL, " +
                "specialty_id INTEGER, " +
                "specialty_name TEXT, " +
                "degree TEXT, " +
                "experience_years INTEGER, " +
                "consultation_fee REAL, " +
                "rating REAL DEFAULT 5.0, " +
                "bio TEXT, " +
                "working_days TEXT, " +
                "is_available INTEGER DEFAULT 1)");

        // 5. Shift Assignments table (Phân ca & Phân y tá theo phòng)
        db.execSQL("CREATE TABLE " + TABLE_SHIFTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ma_ca TEXT, " +
                "ma_bac_si INTEGER, " +
                "ten_bac_si TEXT, " +
                "ma_y_ta INTEGER, " +
                "ten_y_ta TEXT, " +
                "ma_phong TEXT, " +
                "ten_phong TEXT, " +
                "ngay_lam TEXT NOT NULL, " +
                "gio_bat_dau TEXT, " +
                "gio_ket_thuc TEXT, " +
                "trang_thai TEXT DEFAULT 'Đã lên lịch')");

        // 6. Time slots table
        db.execSQL("CREATE TABLE " + TABLE_TIME_SLOTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "doctor_id INTEGER, " +
                "slot_date TEXT NOT NULL, " +
                "slot_time TEXT NOT NULL, " +
                "max_patients INTEGER DEFAULT 3, " +
                "booked_patients INTEGER DEFAULT 0, " +
                "is_available INTEGER DEFAULT 1)");

        // 7. Appointments table
        db.execSQL("CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_code TEXT UNIQUE NOT NULL, " +
                "patient_id INTEGER, " +
                "patient_name TEXT, " +
                "patient_phone TEXT, " +
                "doctor_id INTEGER, " +
                "doctor_name TEXT, " +
                "specialty_name TEXT, " +
                "appointment_date TEXT NOT NULL, " +
                "time_slot TEXT NOT NULL, " +
                "room_code TEXT DEFAULT 'P101', " +
                "symptoms TEXT, " +
                "status TEXT DEFAULT 'PENDING', " +
                "appointment_type TEXT DEFAULT 'Khám theo hẹn', " +
                "queue_number INTEGER DEFAULT 0, " +
                "call_count INTEGER DEFAULT 0, " +
                "arrival_time TEXT, " +
                "cancel_reason TEXT, " +
                "fee REAL DEFAULT 0, " +
                "is_paid INTEGER DEFAULT 0, " +
                "created_at TEXT)");

        // 8. Vital signs table (Menu 3.8 do Y tá nhập)
        db.execSQL("CREATE TABLE " + TABLE_VITAL_SIGNS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER UNIQUE, " +
                "patient_id INTEGER, " +
                "height REAL, " +
                "weight REAL, " +
                "temperature REAL, " +
                "blood_pressure TEXT, " +
                "heart_rate INTEGER, " +
                "spo2 INTEGER, " +
                "other_notes TEXT, " +
                "warning_alert TEXT, " +
                "measured_by TEXT, " +
                "measured_at TEXT)");

        // 9. Medical records table
        db.execSQL("CREATE TABLE " + TABLE_MEDICAL_RECORDS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER UNIQUE, " +
                "patient_id INTEGER, " +
                "doctor_id INTEGER, " +
                "symptoms TEXT, " +
                "patient_medical_history TEXT, " +
                "family_history TEXT, " +
                "diagnosis TEXT, " +
                "differential_diagnosis TEXT, " +
                "notes TEXT, " +
                "tests_required TEXT, " +
                "test_results TEXT, " +
                "follow_up_date TEXT, " +
                "follow_up_reason TEXT, " +
                "referral_specialty TEXT, " +
                "prescription_json TEXT, " +
                "created_at TEXT, " +
                "updated_at TEXT)");

        // 10. Reviews table
        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER UNIQUE, " +
                "patient_id INTEGER, " +
                "patient_name TEXT, " +
                "doctor_id INTEGER, " +
                "rating REAL, " +
                "comment TEXT, " +
                "created_at TEXT)");

        // 11. Invoices table
        db.execSQL("CREATE TABLE " + TABLE_INVOICES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER UNIQUE, " +
                "appointment_code TEXT, " +
                "patient_id INTEGER, " +
                "patient_name TEXT, " +
                "consultation_fee REAL, " +
                "medicine_fee REAL, " +
                "test_fee REAL, " +
                "total_amount REAL, " +
                "status TEXT DEFAULT 'UNPAID', " +
                "payment_method TEXT, " +
                "created_at TEXT)");

        // 12. Notifications table (Menu 1.7)
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "type TEXT DEFAULT 'SYSTEM', " +
                "is_read INTEGER DEFAULT 0, " +
                "created_at TEXT)");

        // 13. Audit logs table (Menu 5.6)
        db.execSQL("CREATE TABLE " + TABLE_AUDIT_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "user_name TEXT, " +
                "user_role TEXT, " +
                "action TEXT, " +
                "details TEXT, " +
                "timestamp TEXT)");

        // 14. System configs table
        db.execSQL("CREATE TABLE " + TABLE_CONFIGS + " (" +
                "key TEXT PRIMARY KEY, " +
                "value TEXT)");

        // Seed initial data for Phòng Khám Lê Lợi
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPECIALTIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHIFTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIME_SLOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VITAL_SIGNS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAL_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIT_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIGS);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        // 1. Configs Phòng Khám Tư Nhân Lê Lợi - Cơ Xương Khớp
        db.execSQL("INSERT INTO " + TABLE_CONFIGS + " VALUES ('MAX_PATIENTS_PER_SLOT', '3')");
        db.execSQL("INSERT INTO " + TABLE_CONFIGS + " VALUES ('MIN_HOURS_BEFORE_CANCEL', '2')");
        db.execSQL("INSERT INTO " + TABLE_CONFIGS + " VALUES ('CLINIC_NAME', 'Phòng Khám Lê Lợi – Cơ Xương Khớp')");
        db.execSQL("INSERT INTO " + TABLE_CONFIGS + " VALUES ('CLINIC_PHONE', '028.3822.8888 - 0900.000.001')");
        db.execSQL("INSERT INTO " + TABLE_CONFIGS + " VALUES ('CLINIC_ADDRESS', 'Số 12 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh')");

        // 2. Demo Users (4 vai trò chuẩn phòng khám tư nhân: ADMIN, DOCTOR, STAFF, PATIENT)
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(1, 'ND001', 'Chủ Phòng Khám Lê Lợi', 'admin@gmail.com', '0900000001', '123456', 'ADMIN')");

        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(2, 'ND002', 'BS. CKI Phan Ba Hai Bình', 'bacsi@gmail.com', '0900000002', '123456', 'DOCTOR')");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(3, 'ND003', 'ThS. BS Trần Thị Mai', 'bsmai@gmail.com', '0900000003', '123456', 'DOCTOR')");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(4, 'ND004', 'PGS. TS Lê Hoàng Quân', 'bsquan@gmail.com', '0900000004', '123456', 'DOCTOR')");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(5, 'ND005', 'BS. CKII Nguyễn Văn Hùng', 'bshung@gmail.com', '0900000008', '123456', 'DOCTOR')");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(6, 'ND006', 'ThS. BS Phạm Thu Trang', 'bstrang@gmail.com', '0900000009', '123456', 'DOCTOR')");

        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(7, 'ND007', 'Lễ Tân Lê Thị Hương', 'letan@gmail.com', '0900000005', '123456', 'STAFF')");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role) VALUES " +
                "(8, 'ND008', 'Lễ Tân Trần Minh Quân', 'letan2@gmail.com', '0900000007', '123456', 'STAFF')");

        // Patient User
        db.execSQL("INSERT INTO " + TABLE_USERS + " (id, user_code, full_name, email, phone, password, role, birth_date, gender, address, insurance_number, medical_history, allergies) VALUES " +
                "(9, 'ND009', 'Nguyễn Hữu Hậu', 'benhnhan@gmail.com', '0912345678', '123456', 'PATIENT', '1998-05-15', 'Nam', 'Số 12 Lê Lợi, Phường Bến Nghé, Quận 1, TP. HCM', 'DN4791234567890', 'Thoát vị đĩa đệm cột sống thắt lưng L4-L5, đau mỏi thắt lưng mãn tính', 'Không ghi nhận dị ứng thuốc')");

        // 3. 7 Dịch vụ Cơ Xương Khớp chuẩn (Section XVIII)
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(1, 'DV01', 'Khám Tổng Quát Cơ Xương Khớp', 'Khám tầm soát hệ vận động, đau nhức xương khớp và cột sống', 200000, 'ic_general')");
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(2, 'DV02', 'Khám & Trị Liệu Cột Sống - Thoát Vị Đĩa Đệm', 'Chuyên sâu thoát vị đĩa đệm thắt lưng L4-L5, thoái hóa cột sống', 350000, 'ic_spine')");
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(3, 'DV03', 'Khám & Điều Trị Đau Cổ Vai Gáy', 'Điều trị đau cổ vai gáy cấp và mãn tính, hội chứng chèn ép rễ', 250000, 'ic_neck')");
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(4, 'DV04', 'Khám & Điều Trị Thoái Hóa Khớp', 'Điều trị thoái hóa khớp háng, khớp cổ chân, viêm bao gân', 300000, 'ic_joint')");
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(5, 'DV05', 'Bệnh Lý Khớp Gối & Tiêm Acid Hyaluronic / Huyết Tương', 'Điều trị tràn dịch gối, tiêm chất nhờn sinh học bôi trơn khớp và PRP', 400000, 'ic_knee')");
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(6, 'DV06', 'Đo Mật Độ Xương & Điều Trị Loãng Xương', 'Tầm soát loãng xương DEXA, phác đồ bổ sung khoáng chất và phòng gãy xương', 300000, 'ic_bone')");
        db.execSQL("INSERT INTO " + TABLE_SPECIALTIES + " (id, specialty_code, name, description, standard_fee, icon_name) VALUES " +
                "(7, 'DV07', 'Tái Khám Cơ Xương Khớp & Vật Lý Trị Liệu', 'Tái khám theo dõi phác đồ, vật lý trị liệu phục hồi chức năng', 150000, 'ic_rehab')");

        // 4. 4 Phòng khám chuyên khoa (Section XVIII)
        db.execSQL("INSERT INTO " + TABLE_ROOMS + " VALUES ('P101', 'Phòng Khám Cột Sống & Cổ Vai Gáy', 'Khám Cột Sống & Đĩa Đệm', 'Đang hoạt động')");
        db.execSQL("INSERT INTO " + TABLE_ROOMS + " VALUES ('P102', 'Phòng Khám Thoái Hóa Khớp & Khớp Gối', 'Khám Thoái Hóa & Khớp Gối', 'Đang hoạt động')");
        db.execSQL("INSERT INTO " + TABLE_ROOMS + " VALUES ('P103', 'Phòng Khám Loãng Xương & Cơ Xương Khớp Tổng Quát', 'Tầm Soát DEXA & Tổng Quát', 'Đang hoạt động')");
        db.execSQL("INSERT INTO " + TABLE_ROOMS + " VALUES ('P104', 'Phòng Can Thiệp & Thủ Thuật Khớp / Vật Lý Trị Liệu', 'Thủ Thuật & Trị Liệu', 'Đang hoạt động')");

        // 5. 5 Bác sĩ chuyên khoa Cơ Xương Khớp
        db.execSQL("INSERT INTO " + TABLE_DOCTORS + " (id, user_id, full_name, specialty_id, specialty_name, degree, experience_years, consultation_fee, rating, bio, working_days, is_available) VALUES " +
                "(1, 2, 'BS. CKI Phan Ba Hai Bình', 1, 'Khám Tổng Quát Cơ Xương Khớp', 'Chuyên Khoa I', 12, 200000, 4.9, 'Bác sĩ có hơn 12 năm kinh nghiệm khám chữa Cơ Xương Khớp tại BV Chợ Rẫy, tận tâm và chu đáo.', 'Thứ 2 - Thứ 7', 1)");
        db.execSQL("INSERT INTO " + TABLE_DOCTORS + " (id, user_id, full_name, specialty_id, specialty_name, degree, experience_years, consultation_fee, rating, bio, working_days, is_available) VALUES " +
                "(2, 3, 'ThS. BS Trần Thị Mai', 2, 'Khám & Trị Liệu Cột Sống - Thoát Vị Đĩa Đệm', 'Thạc Sĩ', 9, 350000, 4.9, 'Chuyên gia điều trị bảo tồn thoát vị đĩa đệm và thoái hóa cột sống không phẫu thuật.', 'Thứ 2 - Thứ 6', 1)");
        db.execSQL("INSERT INTO " + TABLE_DOCTORS + " (id, user_id, full_name, specialty_id, specialty_name, degree, experience_years, consultation_fee, rating, bio, working_days, is_available) VALUES " +
                "(3, 4, 'PGS. TS Lê Hoàng Quân', 5, 'Bệnh Lý Khớp Gối & Tiêm Acid Hyaluronic / Huyết Tương', 'Phó Giáo Sư - Tiến Sĩ', 20, 400000, 5.0, 'Chuyên gia đầu ngành cơ xương khớp và can thiệp khớp gối kỹ thuật cao, nguyên Trưởng khoa Chấn thương Chỉnh hình.', 'Thứ 3, 5, 7', 1)");
        db.execSQL("INSERT INTO " + TABLE_DOCTORS + " (id, user_id, full_name, specialty_id, specialty_name, degree, experience_years, consultation_fee, rating, bio, working_days, is_available) VALUES " +
                "(4, 5, 'BS. CKII Nguyễn Văn Hùng', 3, 'Khám & Điều Trị Đau Cổ Vai Gáy', 'Chuyên Khoa II', 15, 250000, 4.8, 'Chuyên gia điều trị hội chứng cổ vai gáy, chèn ép dây thần kinh cơ và thoái hóa đốt sống cổ.', 'Thứ 2, 4, 6, CN', 1)");
        db.execSQL("INSERT INTO " + TABLE_DOCTORS + " (id, user_id, full_name, specialty_id, specialty_name, degree, experience_years, consultation_fee, rating, bio, working_days, is_available) VALUES " +
                "(5, 6, 'ThS. BS Phạm Thu Trang', 6, 'Đo Mật Độ Xương & Điều Trị Loãng Xương', 'Thạc Sĩ', 8, 300000, 4.9, 'Chuyên gia chẩn đoán loãng xương bằng máy DEXA và phác đồ phục hồi chức năng xương khớp.', 'Thứ 2 - Thứ 7', 1)");

        // 6. Phân ca làm việc phòng khám tư nhân (Ca Sáng: 08:00 - 12:00, Ca Chiều/Tối: 15:00 - 20:00)
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String today = dateFmt.format(cal.getTime());

        db.execSQL("INSERT INTO " + TABLE_SHIFTS + " (ma_ca, ma_bac_si, ten_bac_si, ma_y_ta, ten_y_ta, ma_phong, ten_phong, ngay_lam, gio_bat_dau, gio_ket_thuc, trang_thai) VALUES " +
                "('CA_SANG', 1, 'BS. CKI Phan Ba Hai Bình', 7, 'Lễ Tân Lê Thị Hương', 'P101', 'Phòng Khám Cột Sống & Cổ Vai Gáy', '" + today + "', '08:00', '12:00', 'Đang diễn ra')");
        db.execSQL("INSERT INTO " + TABLE_SHIFTS + " (ma_ca, ma_bac_si, ten_bac_si, ma_y_ta, ten_y_ta, ma_phong, ten_phong, ngay_lam, gio_bat_dau, gio_ket_thuc, trang_thai) VALUES " +
                "('CA_SANG', 2, 'ThS. BS Trần Thị Mai', 7, 'Lễ Tân Lê Thị Hương', 'P102', 'Phòng Khám Thoái Hóa Khớp & Khớp Gối', '" + today + "', '08:00', '12:00', 'Đang diễn ra')");
        db.execSQL("INSERT INTO " + TABLE_SHIFTS + " (ma_ca, ma_bac_si, ten_bac_si, ma_y_ta, ten_y_ta, ma_phong, ten_phong, ngay_lam, gio_bat_dau, gio_ket_thuc, trang_thai) VALUES " +
                "('CA_TOI', 3, 'PGS. TS Lê Hoàng Quân', 8, 'Lễ Tân Trần Minh Quân', 'P103', 'Phòng Khám Loãng Xương & Cơ Xương Khớp Tổng Quát', '" + today + "', '15:00', '20:00', 'Đã lên lịch')");

        // 7. Time Slots phòng khám tư nhân (Ca Sáng 08:00-12:00 & Ca Chiều Tối 15:00-20:00)
        cal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = dateFmt.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        String dayAfter = dateFmt.format(cal.getTime());

        String[] slots = {
                "08:00 - 08:30", "08:30 - 09:00", "09:00 - 09:30", "09:30 - 10:00",
                "10:00 - 10:30", "10:30 - 11:00", "11:00 - 11:30", "11:30 - 12:00",
                "15:00 - 15:30", "15:30 - 16:00", "16:00 - 16:30", "16:30 - 17:00",
                "17:00 - 17:30", "17:30 - 18:00", "18:00 - 18:30", "18:30 - 19:00",
                "19:00 - 19:30", "19:30 - 20:00"
        };

        for (int docId = 1; docId <= 5; docId++) {
            for (String day : new String[]{today, tomorrow, dayAfter}) {
                for (String slot : slots) {
                    db.execSQL("INSERT INTO " + TABLE_TIME_SLOTS + " (doctor_id, slot_date, slot_time, max_patients, booked_patients, is_available) VALUES " +
                            "(" + docId + ", '" + day + "', '" + slot + "', 3, 0, 1)");
                }
            }
        }

        // 8. Sample Appointment (Bệnh nhân Hậu đã đến và được cấp STT #1)
        db.execSQL("INSERT INTO " + TABLE_APPOINTMENTS + " (id, appointment_code, patient_id, patient_name, patient_phone, doctor_id, doctor_name, specialty_name, appointment_date, time_slot, room_code, symptoms, status, appointment_type, queue_number, call_count, arrival_time, fee, is_paid, created_at) VALUES " +
                "(1, 'PK-2026-8891', 9, 'Nguyễn Hữu Hậu', '0912345678', 1, 'BS. CKI Phan Ba Hai Bình', 'Khám & Trị Liệu Cột Sống - Thoát Vị Đĩa Đệm', '" + today + "', '08:30 - 09:00', 'P101', 'Đau thắt lưng âm ỉ lan xuống chân trái, tê bì bắp chân khi ngồi lâu', 'CONFIRMED', 'Khám theo hẹn', 1, 1, '08:15', 350000, 1, '" + now + "')");

        db.execSQL("UPDATE " + TABLE_TIME_SLOTS + " SET booked_patients = booked_patients + 1 WHERE doctor_id = 1 AND slot_date = '" + today + "' AND slot_time = '08:30 - 09:00'");

        // 9. Sample Vital Signs (Tiếp nhận tại phòng khám tư nhân)
        db.execSQL("INSERT INTO " + TABLE_VITAL_SIGNS + " (appointment_id, patient_id, height, weight, temperature, blood_pressure, heart_rate, spo2, other_notes, warning_alert, measured_by, measured_at) VALUES " +
                "(1, 9, 172.0, 68.0, 36.8, '120/80', 76, 98, 'Bệnh nhân tỉnh táo, dáng đi hơi nghiêng do đau lưng', '', 'Lễ Tân Lê Thị Hương', '" + now + "')");

        // 10. Sample Notifications
        db.execSQL("INSERT INTO " + TABLE_NOTIFICATIONS + " (user_id, title, content, type, is_read, created_at) VALUES " +
                "(9, 'Đặt lịch khám thành công', 'Bạn đã đặt lịch khám thành công với BS. CKI Phan Ba Hai Bình vào ngày " + today + " lúc 08:30 tại Phòng P101.', 'APPOINTMENT', 0, '" + now + "')");
        db.execSQL("INSERT INTO " + TABLE_NOTIFICATIONS + " (user_id, title, content, type, is_read, created_at) VALUES " +
                "(9, 'Chào mừng đến Phòng Khám Lê Lợi – Cơ Xương Khớp', 'Cảm ơn quý khách đã tin tưởng và sử dụng dịch vụ chuyên khoa Cơ Xương Khớp tại Phòng Khám Lê Lợi.', 'SYSTEM', 1, '" + now + "')");

        // 11. Initial Audit Log
        db.execSQL("INSERT INTO " + TABLE_AUDIT_LOGS + " (user_id, user_name, user_role, action, details, timestamp) VALUES " +
                "(1, 'Hệ Thống', 'SYSTEM', 'KHỞI TẠO', 'Khởi tạo dữ liệu Phòng Khám Lê Lợi – Cơ Xương Khớp thành công', '" + now + "')");
    }
}
