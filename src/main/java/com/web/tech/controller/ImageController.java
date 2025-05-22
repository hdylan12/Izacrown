package com.web.tech.controller;

import com.web.tech.model.UserImage;
import com.web.tech.service.UserImageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final UserImageService userImageService;

    // Explicit constructor for dependency injection
    public ImageController(UserImageService userImageService) {
        this.userImageService = userImageService;
    }

    @PostMapping("/upload/{userId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body("Only image files are allowed");
        }

        // Validate file size (5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body("File size must be less than 5MB");
        }

        try {
            UserImage savedImage = userImageService.saveImage(userId, file);
            return ResponseEntity.ok()
                    .body(new ImageUploadResponse(
                            savedImage.getId(),
                            savedImage.getContentType(),
                            savedImage.getFileSize()
                    ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String userId) {
        return userImageService.getImageByUserId(userId)
                .map(image -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                        .contentType(MediaType.parseMediaType(image.getContentType()))
                        .body(image.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteImage(@PathVariable String userId) {
        userImageService.deleteImage(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/info/{userId}")
    public ResponseEntity<ImageInfoResponse> getImageInfo(@PathVariable String userId) {
        return userImageService.getImageByUserId(userId)
                .map(image -> ResponseEntity.ok(new ImageInfoResponse(
                        image.getContentType(),
                        image.getFileSize(),
                        image.getId()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // Response DTOs
    record ImageUploadResponse(String id, String contentType, long size) {}
    record ImageInfoResponse(String contentType, long size, String id) {}
}