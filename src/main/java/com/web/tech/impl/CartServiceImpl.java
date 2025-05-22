package com.web.tech.impl;

import com.web.tech.model.Cart;
import com.web.tech.model.CartItem;
import com.web.tech.model.Products;
import com.web.tech.model.User;
import com.web.tech.repository.CartRepository;
import com.web.tech.service.CartService;
import com.web.tech.service.ProductService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Override
    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.getUserById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    Cart cart = new Cart(user);
                    return cartRepository.save(cart);
                });
    }

    @Override
    public void addToCart(String userId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Cart cart = getOrCreateCart(userId);
        Products product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        cart.addProduct(product, quantity);
        cartRepository.save(cart);
    }

    @Override
    public long getCartCount(String userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cart.getItems().stream().mapToLong(CartItem::getQuantity).sum())
                .orElse(0L);
    }

    @Override
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    @Override
    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }
}