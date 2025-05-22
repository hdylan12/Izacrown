package com.web.tech.service;

import com.web.tech.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserRegistrationService {
    User registerUser(User user, MultipartFile profileImage) throws IOException;
}