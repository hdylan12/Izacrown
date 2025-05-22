package com.web.tech.service;

import com.web.tech.model.Products;
import com.web.tech.model.User;
import com.web.tech.repository.ProductRepository;
import com.web.tech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.web.tech.repository.ClientRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Products> getProductById(String id) {
        return productRepository.findById(id);
    }

    public void saveProduct(Products product, MultipartFile image) throws IOException {
        if (product.getId() == null) {
            product.onCreate();
        } else {
            product.onUpdate();
        }
        product = productRepository.save(product);
        if (image != null && !image.isEmpty()) {
            System.out.println("Saving image for product ID: " + product.getId());
            productImageService.saveImage(product.getId(), image);
            product.setImageId(product.getId());
            productRepository.save(product);
        } else {
            System.err.println("No image provided for product ID: " + product.getId());
        }
    }

    public List<Products> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Products> searchProducts(String name, Double minPrice, Double maxPrice) {
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        if (min != null && max != null) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
            }
        }

        if (name != null && !name.trim().isEmpty() && min != null && max != null) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                    name.trim(), min, max);
        } else if (name != null && !name.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(name.trim());
        } else if (min != null && max != null) {
            return productRepository.findByPriceBetween(min, max);
        }

        return getAllProducts();
    }

    public Page<Products> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Products> searchProducts(String name, Double minPrice, Double maxPrice, Pageable pageable) {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }

        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        if (min != null && max != null) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
            }
        }

        if (name != null && !name.trim().isEmpty() && min != null && max != null) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                    name.trim(), min, max, pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else if (min != null && max != null) {
            return productRepository.findByPriceBetween(min, max, pageable);
        }

        return productRepository.findAll(pageable);
    }

    public void deleteProduct(String id) {
        Optional<Products> productOptional = productRepository.findById(id);
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }

        productRepository.deleteById(id);
        try {
            productImageService.deleteImage(id);
        } catch (IOException e) {
            throw new IllegalStateException("Product deleted, but failed to delete associated image: " + e.getMessage());
        }
    }

    public List<User> getAllClients() {
        return userRepository.findAll();
    }

    public Long countAllArtworks() {
        return productRepository.count();
    }

    public Long countAllClients() {
        Long count = userRepository.countByRoleNot("ADMIN");
        System.out.println("Total non-admin users count: " + count);
        if (count == null || count == 0) {
            System.err.println("Warning: No non-admin users found in the database");
        }
        return count != null ? count : 0L;
    }

    public String getProductImageBase64(String productId) throws IOException {
        return productImageService.getProductImageBase64(productId);
    }

    public String getProductImageContentType(String productId) {
        return productImageService.getProductImageContentType(productId);
    }

    public String getProductImage(String productId) {
        String image = productImageService.getProductImage(productId);
        return image != null ? image : "/images/default-product.jpg";
    }

    public Map<String, Long> getArtworksByCategory() {
        List<Products> products = productRepository.findAll();
        Map<String, Long> categoryCounts = products.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Products::getCategory,
                        Collectors.counting()
                ));
        return new LinkedHashMap<>(categoryCounts);
    }

    public void updateProduct(Products product) {
        if (product.getId() != null) {
            product.onUpdate();
        }
        productRepository.save(product);
    }

    public Long countOutOfStock() {
        return productRepository.countByStockLessThanEqual(0);
    }

    public Page<Products> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}