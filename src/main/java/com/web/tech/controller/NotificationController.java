package com.web.tech.controller;

import com.web.tech.model.Notification;
import com.web.tech.model.User;
import com.web.tech.service.CartService;
import com.web.tech.service.NotificationService;
import com.web.tech.service.UserImageService;
import com.web.tech.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserImageService userImageService;

    @GetMapping("/notifications")
    public String getNotifications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Notifications - Current auth: {}", auth);
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Notifications - No authenticated user found");
            return "redirect:/login";
        }

        String email = auth.getName().toLowerCase();
        User user = userService.findByEmail(email);
        if (user == null) {
            logger.warn("Notifications - User not found for email: {}", email);
            return "redirect:/login";
        }

        logger.debug("Notifications - User found: {}, Role: {}", user.getEmail(), user.getRole());
        model.addAttribute("user", user);

        List<Notification> notifications = notificationService.getRecentNotifications(user);
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user);
        long unreadCount = unreadNotifications.size();
        logger.debug("Notifications - Total: {}, Unread: {}", notifications.size(), unreadCount);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("cartCount", cartService.getCartCount(user.getId()));

        String profileImageBase64 = userImageService.getProfileImageBase64(user.getId());
        String contentType = userImageService.getProfileImageContentType(user.getId());
        if (profileImageBase64 != null) {
            model.addAttribute("profileImage", "data:" + contentType + ";base64," + profileImageBase64);
        } else {
            model.addAttribute("profileImage", "/images/default-profile.jpg");
        }

        model.addAttribute("pageTitle", "Notifications");
        return "notifications";
    }

    @GetMapping("/notifications/unread")
    public @ResponseBody List<Notification> getUnreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Unread Notifications - No authenticated user found");
            return List.of();
        }

        String email = auth.getName().toLowerCase();
        User user = userService.findByEmail(email);
        if (user == null) {
            logger.warn("Unread Notifications - User not found for email: {}", email);
            return List.of();
        }

        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user);
        logger.debug("Unread Notifications - Count: {}", unreadNotifications.size());
        return unreadNotifications.stream()
                .filter(notification -> !notification.isRead())
                .collect(Collectors.toList());
    }

    @PostMapping("/notifications/mark-read")
    public String markNotificationRead(@RequestParam("notificationId") String notificationId) {
        logger.debug("Marking notification as read: {}", notificationId);
        notificationService.markAsRead(notificationId);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName().toLowerCase();
            User user = userService.findByEmail(email);
            if (user != null) {
                logger.debug("Marking all notifications as read for user: {}", user.getEmail());
                notificationService.markAllAsRead(user);
            }
        }
        return "redirect:/notifications";
    }
}