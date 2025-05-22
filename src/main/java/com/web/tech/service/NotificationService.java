package com.web.tech.service;

import com.web.tech.model.Notification;
import com.web.tech.model.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(String title, String message, String type, User user);
    List<Notification> getRecentNotifications(User user);
    List<Notification> getUnreadNotifications(User user);
    void markAsRead(String notificationId);
    void markAllAsRead(User user);
}