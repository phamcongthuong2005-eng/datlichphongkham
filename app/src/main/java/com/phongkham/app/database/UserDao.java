package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final DatabaseHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public User login(String emailOrPhone, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + 
                " WHERE (email = ? OR phone = ?) AND password = ?", new String[]{emailOrPhone, emailOrPhone, password});

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public boolean register(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("password", user.getPassword());
        values.put("role", user.getRole() != null ? user.getRole() : User.ROLE_PATIENT);
        values.put("birth_date", user.getBirthDate());
        values.put("gender", user.getGender());
        values.put("address", user.getAddress());
        values.put("insurance_number", user.getInsuranceNumber());
        values.put("medical_history", user.getMedicalHistory());
        values.put("allergies", user.getAllergies());
        values.put("is_locked", 0);

        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        if (id != -1) {
            user.setId((int) id);
            return true;
        }
        return false;
    }

    public boolean updateUserProfile(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("birth_date", user.getBirthDate());
        values.put("gender", user.getGender());
        values.put("address", user.getAddress());
        values.put("insurance_number", user.getInsuranceNumber());
        values.put("medical_history", user.getMedicalHistory());
        values.put("allergies", user.getAllergies());

        int rows = db.update(DatabaseHelper.TABLE_USERS, values, "id = ?", new String[]{String.valueOf(user.getId())});
        return rows > 0;
    }

    public boolean updateProfile(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("password", user.getPassword());
        values.put("birth_date", user.getBirthDate());
        values.put("gender", user.getGender());
        values.put("address", user.getAddress());
        values.put("insurance_number", user.getInsuranceNumber());
        values.put("medical_history", user.getMedicalHistory());
        values.put("allergies", user.getAllergies());

        int rows = db.update(DatabaseHelper.TABLE_USERS, values, "id = ?", new String[]{String.valueOf(user.getId())});
        return rows > 0;
    }

    public boolean changePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int rows = db.update(DatabaseHelper.TABLE_USERS, values, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean resetPasswordByPhoneOrEmail(String phoneOrEmail, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int rows = db.update(DatabaseHelper.TABLE_USERS, values, "email = ? OR phone = ?", new String[]{phoneOrEmail, phoneOrEmail});
        return rows > 0;
    }

    public boolean toggleLockUser(int userId, boolean lock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_locked", lock ? 1 : 0);
        int rows = db.update(DatabaseHelper.TABLE_USERS, values, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE id = ?", new String[]{String.valueOf(id)});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + " ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            list.add(cursorToUser(cursor));
        }
        cursor.close();
        return list;
    }

    public List<User> searchPatients(String keyword) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "%" + keyword + "%";
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + 
                " WHERE role = 'PATIENT' AND (full_name LIKE ? OR phone LIKE ? OR insurance_number LIKE ?)", 
                new String[]{query, query, query});
        while (cursor.moveToNext()) {
            list.add(cursorToUser(cursor));
        }
        cursor.close();
        return list;
    }

    private User cursorToUser(Cursor cursor) {
        User u = new User();
        u.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        u.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        u.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        u.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        u.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        u.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow("birth_date")));
        u.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        u.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
        u.setInsuranceNumber(cursor.getString(cursor.getColumnIndexOrThrow("insurance_number")));
        u.setMedicalHistory(cursor.getString(cursor.getColumnIndexOrThrow("medical_history")));
        u.setAllergies(cursor.getString(cursor.getColumnIndexOrThrow("allergies")));
        u.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow("is_locked")) == 1);
        return u;
    }
}
