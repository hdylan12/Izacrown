package com.web.tech.controller;

import com.web.tech.model.Cart;
import com.web.tech.model.CartItem;
import com.web.tech.model.Products;
import com.web.tech.model.User;
import com.web.tech.service.CartService;
import com.web.tech.service.ProductService;
import com.web.tech.service.UserImageService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserImageService userImageService;

    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Optional<User> userOptional = Optional.ofNullable(userService.findByEmail(auth.getName().toLowerCase()));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                String profilePicture = userImageService.getProfileImageBase64(user.getId());
                String contentType = userImageService.getProfileImageContentType(user.getId());
                model.addAttribute("profilePicture",
                        profilePicture != null && contentType != null
                                ? "data:" + contentType + ";base64," + profilePicture
                                : "/images/default-avatar.jpg");
            } else {
                model.addAttribute("user", null);
                model.addAttribute("profilePicture", "/images/default-avatar.jpg");
            }
        } else {
            model.addAttribute("user", null);
            model.addAttribute("profilePicture", "/images/default-avatar.jpg");
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            Cart cart = cartService.getOrCreateCart(userId);
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Map<String, String> productImageBase64 = new HashMap<>();
            cart.getItems().forEach(item -> {
                String productId = item.getProduct().getId();
                String image = productService.getProductImage(productId);
                productImageBase64.put(productId, image);
            });

            model.addAttribute("cart", cart);
            model.addAttribute("cartCount", cartService.getCartCount(userId));
            model.addAttribute("user", user);
            String profilePicture = userImageService.getProfileImageBase64(userId);
            model.addAttribute("profilePicture", profilePicture != null ? profilePicture : "/images/default-avatar.jpg");
            model.addAttribute("orderCount", 0); // Replace with actual service call
            model.addAttribute("wishlistCount", 0); // Replace with actual service call
            model.addAttribute("totalSpent", 0); // Replace with actual service call
            model.addAttribute("productImageBase64", productImageBase64);

            return "admin/user/cart";
        } catch (IllegalArgumentException e) {
            return "redirect:/login?error=" + e.getMessage();
        } catch (Exception e) {
            System.err.println("Error in viewCart: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load cart", e);
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/login";
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            Products product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (product.getStock() < quantity) {
                return "error: Insufficient stock: only " + product.getStock() + " items left";
            }

            cartService.addToCart(userId, productId, quantity);
            product.setStock(product.getStock() - quantity);
            productService.updateProduct(product);

            return "success";
        } catch (IllegalArgumentException e) {
            return "error: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return "error: Internal server error";
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public long getCartCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return 0;
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            return cartService.getCartCount(userId);
        } catch (IllegalArgumentException e) {
            System.err.println("Error getting cart count: " + e.getMessage());
            return 0;
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public String updateCartItem(
            @RequestParam String itemId,
            @RequestParam String productId,
            @RequestParam int quantity,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/login";
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            Cart cart = cartService.getOrCreateCart(userId);
            if (cart.getItems().stream().noneMatch(item -> item.getProduct().getId().equals(productId))) {
                throw new IllegalArgumentException("Product not found in cart");
            }
            Products product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            int currentQuantity = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .mapToInt(CartItem::getQuantity)
                    .sum();

            int stockDifference = quantity - currentQuantity;
            if (stockDifference > 0 && product.getStock() < stockDifference) {
                return "error: Insufficient stock: only " + product.getStock() + " items left";
            }

            cart.updateProductQuantity(productId, quantity);
            product.setStock(product.getStock() - stockDifference);
            productService.updateProduct(product);
            cartService.saveCart(cart);

            return "success";
        } catch (IllegalArgumentException e) {
            return "error: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Error updating cart: " + e.getClass().getName() + ": " + e.getMessage());
            return "error: Internal server error";
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public String removeCartItem(
            @RequestParam String itemId,
            @RequestParam String productId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return "redirect:/login";
        }
        try {
            String userId = getUserIdFromAuthentication(authentication);
            Cart cart = cartService.getOrCreateCart(userId);
            if (cart.getItems().stream().noneMatch(item -> item.getProduct().getId().equals(productId))) {
                throw new IllegalArgumentException("Product not found in cart");
            }
            int removedQuantity = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .mapToInt(CartItem::getQuantity)
                    .sum();
            cart.removeProduct(productId);
            Products product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            product.setStock(product.getStock() + removedQuantity);
            productService.updateProduct(product);
            cartService.saveCart(cart);
            return "success";
        } catch (IllegalArgumentException e) {
            return "error: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Error removing cart item: " + e.getClass().getName() + ": " + e.getMessage());
            return "error: Internal server error";
        }
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        System.out.println("Email: " + email);
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found for email: " + email);
        }
        return user.getId();
    }
}