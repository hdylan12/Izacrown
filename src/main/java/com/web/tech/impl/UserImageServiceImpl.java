package com.web.tech.impl;

import com.web.tech.model.UserImage;
import com.web.tech.repository.UserImageRepository;
import com.web.tech.service.UserImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserImageServiceImpl implements UserImageService {

    @Autowired
    private UserImageRepository userImageRepository;

    @Override
    public UserImage saveImage(String userId, MultipartFile file) throws IOException {
        UserImage userImage = new UserImage();
        userImage.setUserId(userId);
        userImage.setImageData(file.getBytes());
        userImage.setContentType(file.getContentType());
        userImage.setFileSize(file.getSize());
        return userImageRepository.save(userImage);
    }

    @Override
    public UserImage updateImage(String userId, MultipartFile file) throws IOException {
        if (userImageRepository.existsByUserId(userId)) {
            userImageRepository.deleteByUserId(userId);
        }
        return saveImage(userId, file);
    }

    @Override
    public Optional<UserImage> getImage(String userId) {
        return userImageRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserImage> getImageByUserId(String userId) {
        return userImageRepository.findByUserId(userId);
    }

    @Override
    public void deleteImage(String userId) {
        userImageRepository.deleteByUserId(userId);
    }

    @Override
    public String getProfileImageBase64(String userId) {
        Optional<UserImage> userImageOpt = userImageRepository.findByUserId(userId);
        if (userImageOpt.isPresent()) {
            UserImage userImage = userImageOpt.get();
            return Base64.getEncoder().encodeToString(userImage.getImageData());
        }
        return null;
    }

    @Override
    public String getProfileImageContentType(String userId) {
        Optional<UserImage> userImageOpt = userImageRepository.findByUserId(userId);
        return userImageOpt.map(UserImage::getContentType).orElse("image/jpeg");
    }
}