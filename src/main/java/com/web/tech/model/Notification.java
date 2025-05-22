package com.web.tech.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String title;

    private String message;

    private String type;

    private LocalDateTime createdAt;

    private boolean isRead;

    @DBRef
    private User user;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Notification(String title, String message, String type, LocalDateTime createdAt, boolean isRead, User user) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.user = user;
    }

    public Notification(String title, String message, String type, User user) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.user = user;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}