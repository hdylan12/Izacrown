package com.web.tech.controller;

import com.web.tech.model.ProductImages;
import com.web.tech.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Autowired
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String productId,
            @RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File size must be less than 5MB");
        }
        try {
            ProductImages savedImage = productImageService.saveImage(productId, file);
            return ResponseEntity.ok().body(savedImage);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error processing image: " + e.getMessage());
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            return productImageService.getImageByProductId(productId)
                    .map(image -> ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                            .contentType(MediaType.parseMediaType(image.getContentType()))
                            .body(image.getImageData()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteImage(@PathVariable String productId) throws IOException {
        productImageService.deleteImage(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metadata/{productId}")
    public ResponseEntity<ProductImages> getImageMetadata(@PathVariable String productId) {
        return productImageService.getImageByProductId(productId)
                .map(image -> {
                    ProductImages metadata = new ProductImages();
                    metadata.setId(image.getId());
                    metadata.setProductId(image.getProductId());
                    metadata.setContentType(image.getContentType());
                    metadata.setFileSize(image.getFileSize());
                    metadata.setUploadDate(image.getUploadDate());
                    return ResponseEntity.ok(metadata);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}