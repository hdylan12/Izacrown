package com.web.tech.service;

import com.web.tech.model.Products;
import org.springframework.transaction.annotation.Transactional;

public interface WishlistService {
    long countWishlistItemsByUser(Long userId);

    @Transactional
    void addItem(Long userId, Products product);
}