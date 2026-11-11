package com.phongkham.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.phongkham.app.models.NotificationItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationDao {
    private final DatabaseHelper dbHelper;

    public NotificationDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean addNotification(int userId, String title, String content, String type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = sdf.format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("content", content);
        values.put("type", type);
        values.put("is_read", 0);
        values.put("created_at", now);

        long id = db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, values);
        return id != -1;
    }

    public List<NotificationItem> getNotificationsByUserId(int userId) {
        return getNotifications(userId, "ALL");
    }

    public List<NotificationItem> getNotifications(int userId, String filter) {
        List<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        String[] args;

        if ("UNREAD".equals(filter)) {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTIFICATIONS + " WHERE user_id = ? AND is_read = 0 ORDER BY id DESC";
            args = new String[]{String.valueOf(userId)};
        } else if ("READ".equals(filter)) {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTIFICATIONS + " WHERE user_id = ? AND is_read = 1 ORDER BY id DESC";
            args = new String[]{String.valueOf(userId)};
        } else {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTIFICATIONS + " WHERE user_id = ? ORDER BY id DESC";
            args = new String[]{String.valueOf(userId)};
        }

        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            NotificationItem item = new NotificationItem();
            item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            item.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            item.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
            item.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
            item.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1);
            item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            list.add(item);
        }
        cursor.close();
        return list;
    }

    public boolean markAsRead(int notificationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        int rows = db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values, "id = ?", new String[]{String.valueOf(notificationId)});
        return rows > 0;
    }

    public boolean markAllAsRead(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        int rows = db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values, "user_id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public int getUnreadCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICATIONS + " WHERE user_id = ? AND is_read = 0", 
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
