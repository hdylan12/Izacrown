package com.web.tech.impl;

import com.web.tech.model.Cart;
import com.web.tech.model.Orders;
import com.web.tech.model.User;
import com.web.tech.repository.OrderRepository;
import com.web.tech.service.NotificationService;
import com.web.tech.service.OrderService;
import com.web.tech.service.UserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    public void init() {
        fixNullOrderDates();
    }

    @Override
    public Orders createOrder(String userId, Cart cart, String shippingAddress, String paymentMethod) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Orders order = Orders.createFromCart(cart, shippingAddress, paymentMethod);
        order.setUser(user);
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        order = orderRepository.save(order);

        user.getOrders().add(order);
        user.setOrderCount(user.getOrderCount() + 1);
        user.setTotalSpent(user.getTotalSpent().add(order.getTotalAmount()));
        userService.save(user);

        String title = "Order Placed";
        String message = String.format("Your order #%s has been successfully placed.", order.getOrderNumber());
        notificationService.createNotification(title, message, "SUCCESS", user);
        logger.info("Notification created for user {}: {}", user.getEmail(), message);

        return order;
    }

    @Override
    public void cancelOrder(String orderNumber, String userId) {
        Orders order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }
        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException("Order cannot be cancelled");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        User user = order.getUser();
        user.setOrderCount(user.getOrderCount() - 1);
        user.setTotalSpent(user.getTotalSpent().subtract(order.getTotalAmount()));
        userService.save(user);

        String title = "Order Cancelled";
        String message = String.format("Your order #%s has been cancelled.", order.getOrderNumber());
        notificationService.createNotification(title, message, "INFO", user);
        logger.info("Notification created for user {}: {}", user.getEmail(), message);
    }

    @Override
    public BigDecimal getTotalSpent(String userId) {
        return userService.getUserById(userId)
                .map(User::getTotalSpent)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public long countAllOrders() {
        return orderRepository.count();
    }

    @Override
    public Map<String, Long> getOrdersByMonth(int months) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        return orderRepository.findAll().stream()
                .filter(order -> {
                    LocalDate date = order.getOrderDate().toLocalDate();
                    return !date.isBefore(start) && !date.isAfter(end);
                })
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().toLocalDate().format(formatter),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    @Override
    public Orders findById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Orders> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        Orders order = findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found.");
        }
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        if (!order.getStatus().equals(status)) {
            order.setStatus(status);
            order.setLastUpdated(LocalDateTime.now());
            orderRepository.save(order);
            User user = order.getUser();
            String title = "Order Status Updated";
            String message = String.format("Your order #%s has been updated to %s.", order.getOrderNumber(), status);
            notificationService.createNotification(title, message, "INFO", user);
            logger.info("Notification created for user {}: {}", user.getEmail(), message);
        } else {
            logger.info("Order #{} status unchanged: {}", order.getOrderNumber(), status);
        }
    }

    private boolean isValidStatus(String status) {
        return status != null && (
                status.equals("PENDING") ||
                        status.equals("PROCESSING") ||
                        status.equals("SHIPPED") ||
                        status.equals("DELIVERED") ||
                        status.equals("CANCELLED")
        );
    }

    public List<Orders> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public void fixNullOrderDates() {
        List<Orders> orders = orderRepository.findAll();
        for (Orders order : orders) {
            if (order.getOrderDate() == null) {
                order.setOrderDate(LocalDateTime.now());
                orderRepository.save(order);
                logger.info("Updated order #{} with null orderDate to {}", order.getOrderNumber(), order.getOrderDate());
            }
        }
    }
}