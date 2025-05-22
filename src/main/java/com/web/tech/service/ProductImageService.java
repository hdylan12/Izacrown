package com.web.tech.service;

import com.web.tech.model.ProductImages;
import com.web.tech.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Service
public class ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    public ProductImages saveImage(String productId, MultipartFile file) throws IOException {
        ProductImages productImage = new ProductImages(
                productId,
                file.getBytes(),
                file.getContentType()
        );
        return productImageRepository.save(productImage);
    }

    public Optional<ProductImages> getImageByProductId(String productId) {
        return productImageRepository.findByProductId(productId);
    }

    public void deleteImage(String productId) throws IOException {
        Optional<ProductImages> imageOptional = productImageRepository.findByProductId(productId);
        if (imageOptional.isPresent()) {
            try {
                productImageRepository.delete(imageOptional.get());
            } catch (Exception e) {
                throw new IOException("Failed to delete image for product ID: " + productId, e);
            }
        }
    }

    public String getProductImageBase64(String productId) {
        Optional<ProductImages> imageOptional = productImageRepository.findByProductId(productId);
        if (imageOptional.isPresent()) {
            byte[] imageData = imageOptional.get().getImageData();
            return Base64.getEncoder().encodeToString(imageData);
        }
        return null;
    }

    public String getProductImageContentType(String productId) {
        return productImageRepository.findByProductId(productId)
                .map(ProductImages::getContentType)
                .orElse("image/png");
    }

    public String getProductImage(String productId) {
        String base64 = getProductImageBase64(productId);
        String contentType = getProductImageContentType(productId);
        return (base64 != null && contentType != null) ?
                "data:" + contentType + ";base64," + base64 :
                "/images/default-product.jpg";
    }
}