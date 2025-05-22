package com.web.tech.impl;

import com.web.tech.model.Notification;
import com.web.tech.model.User;
import com.web.tech.repository.NotificationRepository;
import com.web.tech.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(String title, String message, String type, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for notification");
        }
        Notification notification = new Notification(title, message, type, user);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
}