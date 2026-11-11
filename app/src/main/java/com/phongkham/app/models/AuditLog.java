package com.phongkham.app.models;

import java.io.Serializable;

public class AuditLog implements Serializable {
    private int id;
    private int userId;
    private String userName;
    private String userRole;
    private String action;
    private String details;
    private String timestamp;

    public AuditLog() {}

    public AuditLog(int id, int userId, String userName, String userRole, String action, String details, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
