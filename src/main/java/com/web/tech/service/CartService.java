package com.web.tech.service;

import com.web.tech.model.Cart;
import com.web.tech.model.Products;

public interface CartService {
    Cart getOrCreateCart(String userId);
    void addToCart(String userId, String productId, int quantity);
    long getCartCount(String userId);
    void clearCart(String userId);
    void saveCart(Cart cart);
}