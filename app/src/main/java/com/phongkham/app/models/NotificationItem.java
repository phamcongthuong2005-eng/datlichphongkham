package com.phongkham.app.models;

import java.io.Serializable;

public class NotificationItem implements Serializable {
    public static final String TYPE_APPOINTMENT = "APPOINTMENT";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_QUEUE = "QUEUE";
    public static final String TYPE_RESULT = "RESULT";
    public static final String TYPE_REMINDER = "REMINDER";

    private int id;
    private int userId;
    private String title;
    private String content;
    private String type; // APPOINTMENT, SYSTEM, QUEUE, RESULT, REMINDER
    private boolean isRead;
    private String createdAt;

    public NotificationItem() {}

    public NotificationItem(int id, int userId, String title, String content, String type, boolean isRead, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
