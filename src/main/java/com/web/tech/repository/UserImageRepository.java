package com.web.tech.repository;

import com.web.tech.model.UserImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserImageRepository extends MongoRepository<UserImage, String> {

    // Find by PostgreSQL user ID reference
    Optional<UserImage> findByUserId(String userId);

    // Find all images for multiple users
    List<UserImage> findAllByUserIdIn(List<String> userIds);


    Optional<UserImage> findByUserIdAndContentType(String userId, String contentType);

    // Check if image exists for user
    boolean existsByUserId(String userId);

    // Delete by user reference
    void deleteByUserId(String userId);
}