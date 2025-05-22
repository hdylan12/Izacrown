package com.web.tech.service;

import com.web.tech.model.Cart;
import com.web.tech.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Orders createOrder(String userId, Cart cart, String shippingAddress, String paymentMethod);
    void cancelOrder(String orderNumber, String userId);
    BigDecimal getTotalSpent(String userId);
    long countAllOrders();
    Map<String, Long> getOrdersByMonth(int months);
    Orders findById(String id);
    Page<Orders> getAllOrders(Pageable pageable);
    void updateOrderStatus(String orderId, String status);
    List<Orders> getOrdersByUserId(String userId);
}