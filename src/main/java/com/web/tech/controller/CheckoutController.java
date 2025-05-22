package com.web.tech.controller;

import com.web.tech.model.Cart;
import com.web.tech.model.Orders;
import com.web.tech.model.User;
import com.web.tech.service.CartService;
import com.web.tech.service.OrderService;
import com.web.tech.service.UserService;
import com.web.tech.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CheckoutController {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/checkout")
    public String viewCheckout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            logger.warn("Unauthorized access attempt to checkout");
            return "redirect:/login";
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            logger.info("Checkout page accessed by user ID: {}", userId);

            Cart cart = cartService.getOrCreateCart(userId);
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            model.addAttribute("cart", cart);
            model.addAttribute("user", user);
            model.addAttribute("cartCount", cartService.getCartCount(userId));
            model.addAttribute("shippingAddress", user.getAddress() != null ? user.getAddress() : "");

            return "admin/user/checkout";
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException in viewCheckout: {}", e.getMessage());
            return "redirect:/login?error=" + e.getMessage();
        } catch (Exception e) {
            logger.error("Error in viewCheckout: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load checkout", e);
        }
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @RequestParam String shippingAddress,
            @RequestParam String paymentMethod,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        logger.info("Placing order. Payment method: {}", paymentMethod);

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            logger.warn("Unauthorized order placement attempt");
            return "redirect:/login";
        }

        try {
            String userId = getUserIdFromAuthentication(authentication);
            logger.info("Processing order for user ID: {}", userId);

            Cart cart = cartService.getOrCreateCart(userId);
            logger.info("Cart retrieved. Items count: {}", cart.getItems().size());

            if (cart.getItems().isEmpty()) {
                logger.warn("Attempt to place order with empty cart");
                redirectAttributes.addFlashAttribute("errorMessage", "Cart is empty");
                return "redirect:/checkout";
            }

            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                logger.warn("Missing shipping address in order");
                redirectAttributes.addFlashAttribute("errorMessage", "Shipping address is required");
                return "redirect:/order-confirmation";
            }

            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                logger.warn("Missing payment method in order");
                redirectAttributes.addFlashAttribute("errorMessage", "Payment method is required");
                return "redirect:/checkout";
            }

            logger.info("Creating order with shipping address: {} and payment method: {}",
                    shippingAddress, paymentMethod);

            Orders order = orderService.createOrder(userId, cart, shippingAddress, paymentMethod);
            logger.info("Order created successfully. Order number: {}", order.getOrderNumber());

            cartService.clearCart(userId);
            logger.info("Cart cleared for user ID: {}", userId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + order.getOrderNumber() + " placed successfully!");
            return "redirect:/order-confirmation";
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to place order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-confirmation")
    public String showOrderConfirmation(Model model) {
        return "admin/user/order-confirmation";
    }

    @PostMapping("/checkout/cancel")
    @ResponseBody
    public String cancelOrder(@RequestParam String orderNumber, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "error: Please log in";
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            orderService.cancelOrder(orderNumber, userId);
            return "success: Order cancelled successfully";
        } catch (IllegalArgumentException e) {
            return "error: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Error in cancelOrder: {}", e.getMessage(), e);
            return "error: Failed to cancel order";
        }
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(
            @RequestParam("orderId") String orderId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            Orders order = orderService.findById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Order not found.");
            }
            orderService.updateOrderStatus(orderId, status);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User admin = userService.findByEmail(auth.getName().toLowerCase());
            if (admin == null) {
                throw new IllegalStateException("Admin user not found.");
            }
            notificationService.createNotification(
                    "Order Status Updated",
                    "Order #" + order.getOrderNumber() + " status changed to " + status + ".",
                    "SUCCESS",
                    admin
            );
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating order id: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update order status. Please try again.");
        }
        return "redirect:/admin/user/order-confirmation";
    }

    @GetMapping("/account/orders")
    public String viewOrders(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/login";
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            List<Orders> orders = orderService.getOrdersByUserId(userId);
            List<Map<String, Object>> formattedOrders = orders.stream().map(order -> {
                Map<String, Object> map = new HashMap<>();
                map.put("orderNumber", order.getOrderNumber());
                map.put("orderDate", order.getOrderDate() != null
                        ? order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                        : "N/A");
                map.put("totalAmount", order.getTotalAmount());
                map.put("status", order.getStatus());
                return map;
            }).collect(Collectors.toList());
            model.addAttribute("orders", formattedOrders);
            model.addAttribute("cartCount", cartService.getCartCount(userId));
            return "admin/user/orders";
        } catch (Exception e) {
            logger.error("Error loading orders: {}", e.getMessage(), e);
            return "redirect:/login?error=Failed to load orders";
        }
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found for email: " + email);
        }
        return user.getId();
    }
}