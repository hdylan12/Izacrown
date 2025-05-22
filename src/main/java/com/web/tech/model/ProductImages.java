package com.web.tech.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "product_images")
public class ProductImages {
    @Id
    private String id;

    @Field("product_id")
    private String productId;

    @Field("image_data")
    private byte[] imageData;

    @Field("content_type")
    private String contentType;

    @Field("file_size")
    private long fileSize;

    @Field("upload_date")
    private LocalDateTime uploadDate;

    // Constructors
    public ProductImages() {
        this.uploadDate = LocalDateTime.now();
    }

    public ProductImages(String productId, byte[] imageData, String contentType) {
        this.productId = productId;
        this.imageData = imageData;
        this.contentType = contentType;
        this.fileSize = imageData != null ? imageData.length : 0;
        this.uploadDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
        this.fileSize = imageData != null ? imageData.length : 0;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}