package com.web.tech.repository;

import com.web.tech.model.ProductImages;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductImageRepository extends MongoRepository<ProductImages, String> {
    Optional<ProductImages> findByProductId(String productId);
}