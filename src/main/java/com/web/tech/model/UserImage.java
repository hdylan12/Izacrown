package com.web.tech.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user_images")
public class UserImage {
    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("image_data")
    private byte[] imageData;

    @Field("content_type")
    private String contentType;

    @Field("file_size")
    private long fileSize;

    // Constructors
    public UserImage() {}

    public UserImage(String userId, byte[] imageData, String contentType) {
        this.userId = userId;
        this.imageData = imageData;
        this.contentType = contentType;
        this.fileSize = imageData != null ? imageData.length : 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }
}