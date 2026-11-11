package com.phongkham.app.models;

import java.io.Serializable;

public class Specialty implements Serializable {
    private int id;
    private String name;
    private String description;
    private String iconName;

    public Specialty() {}

    public Specialty(int id, String name, String description, String iconName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}
