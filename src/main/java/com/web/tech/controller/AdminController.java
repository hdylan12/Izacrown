package com.web.tech.controller;

import com.web.tech.model.Orders;
import com.web.tech.model.Products;
import com.web.tech.model.User;
import com.web.tech.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final UserImageService userImageService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @Autowired
    public AdminController(UserService userService, UserImageService userImageService, ProductService productService,
                           CartService cartService, OrderService orderService, NotificationService notificationService) {
        this.userService = userService;
        this.userImageService = userImageService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String adminDashboard(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Admin Dashboard - Current auth: " + auth);
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("Admin Dashboard - No authenticated user found");
            return "redirect:/login";
        }

        String email = auth.getName().toLowerCase();
        User user = userService.findByEmail(email);
        if (user == null) {
            logger.warn("Admin Dashboard - User not found for email: " + email);
            return "redirect:/login";
        }

        logger.debug("Admin Dashboard - User found: " + user.getEmail() + ", Role: " + user.getRole());
        model.addAttribute("user", user);

        if ("USER".equals(user.getRole())) {
            logger.debug("Admin Dashboard - Redirecting ROLE_USER to shop page");
            return "redirect:/shop";
        }

        Long productCount = productService.countAllArtworks();
        Long outOfStockCount = productService.countOutOfStock();
        Long customerCount = productService.countAllClients();
        Long orderCount = orderService.countAllOrders();
        model.addAttribute("productCount", productCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("orderCount", orderCount);

        var notifications = notificationService.getRecentNotifications(user);
        Long unreadCount = notificationService.getUnreadNotifications(user).stream().count();
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        String profileImageBase64 = userImageService.getProfileImageBase64(user.getId());
        String contentType = userImageService.getProfileImageContentType(user.getId());
        if (profileImageBase64 != null) {
            model.addAttribute("profileImage", "data:" + contentType + ";base64," + profileImageBase64);
        } else {
            model.addAttribute("profileImage", "/images/default-profile.jpg");
        }

        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin";
    }

    @PostMapping("/products/{id}/stock")
    public String updateStock(
            @PathVariable("id") String id,
            @RequestParam("stock") int stock,
            RedirectAttributes redirectAttributes) {
        try {
            Products product = productService.getProductById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            int oldStock = product.getStock();
            product.setStock(stock);
            productService.updateProduct(product);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User admin = userService.findByEmail(auth.getName().toLowerCase());
            String message = String.format("Stock for '%s' updated from %d to %d.", product.getName(), oldStock, stock);
            notificationService.createNotification(
                    "Stock Updated",
                    message,
                    "SUCCESS",
                    admin
            );
            redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating stock for product id: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update stock: " + e.getMessage());
        }
        return "redirect:/admin/artworks";
    }

    @PostMapping("/notifications/mark-read")
    public String markNotificationRead(@RequestParam("notificationId") String notificationId) {
        notificationService.markAsRead(notificationId);
        return "redirect:/admin";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName().toLowerCase();
        User user = userService.findByEmail(email);
        if (user != null) {
            notificationService.markAllAsRead(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/delete/client/{id}")
    public String deleteClient(
            @PathVariable("id") String id,
            RedirectAttributes redirectAttributes) {
        try {
            User client = userService.findById(id);
            if (client == null) {
                throw new IllegalArgumentException("Client not found.");
            }
            if ("ADMIN".equals(client.getRole())) {
                throw new IllegalStateException("Cannot delete an admin user.");
            }
            userService.deleteUser(id);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User admin = userService.findByEmail(auth.getName().toLowerCase());
            notificationService.createNotification(
                    "Client Deleted",
                    "Client " + client.getFirstName() + " " + client.getLastName() + " was successfully deleted.",
                    "SUCCESS",
                    admin
            );
            redirectAttributes.addFlashAttribute("successMessage", "Client deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting client id: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error deleting client: " + e.getMessage());
        }
        return "redirect:/admin/clients";
    }

    @GetMapping("/orders")
    public String showOrders(
            @PageableDefault(size = 10, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<Orders> orderPage = orderService.getAllOrders(pageable);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("page", orderPage);
        model.addAttribute("pageTitle", "Orders");
        model.addAttribute("currentPage", "orders");
        return "orders";
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

            User customer = order.getUser();
            if (customer == null) {
                logger.warn("No customer found for order #{}", order.getOrderNumber());
                throw new IllegalStateException("Customer not found for order.");
            }
            User verifiedCustomer = userService.findById(customer.getId());
            if (verifiedCustomer == null) {
                logger.warn("Customer with ID {} not found in clients table for order #{}", customer.getId(), order.getOrderNumber());
                throw new IllegalStateException("Customer not found in database.");
            }
            notificationService.createNotification(
                    "Order Status Updated",
                    "Your order #" + order.getOrderNumber() + " has been updated to " + status + ".",
                    "SUCCESS",
                    verifiedCustomer
            );

            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating order id: {}. Message: {}", orderId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating order id: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update order status. Please try again.");
        }
        return "redirect:/admin/orders";
    }
}