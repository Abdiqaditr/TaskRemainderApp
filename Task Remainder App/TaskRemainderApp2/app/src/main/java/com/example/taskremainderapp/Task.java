package com.example.taskremainderapp;

import java.util.Date;

public class Task {
    private String title;
    private String description;
    private boolean highPriority;
    private boolean alert;
    private Date createdAt;
    private String id;
    private boolean completed; // Added completed field

    public Task() {
        // Empty constructor needed for Firestore
    }

    public Task(String id, String title, String description, boolean highPriority, boolean alert, Date createdAt, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.highPriority = highPriority;
        this.alert = alert;
        this.createdAt = createdAt;
        this.completed = completed;
    }

    // Existing getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public void setHighPriority(boolean highPriority) {
        this.highPriority = highPriority;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // New getter and setter for completed field
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

