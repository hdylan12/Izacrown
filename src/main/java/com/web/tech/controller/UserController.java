package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.model.UserImage;
import com.web.tech.service.UserImageService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class UserController {

    private final UserService userService;
    private final UserImageService userImageService;

    private static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";

    @Autowired
    public UserController(UserService userService, UserImageService userImageService) {
        this.userService = userService;
        this.userImageService = userImageService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "pages/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam("profileImage") MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "This email is already registered");
        } else if (!Pattern.matches(EMAIL_REGEX, user.getEmail())) {
            result.rejectValue("email", "error.user", "Please enter a valid email address");
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        } else if (!Pattern.matches(PASSWORD_REGEX, user.getPassword())) {
            result.rejectValue("password", "error.user",
                    "Password must be 8-20 characters with at least one digit, lowercase, uppercase, and special character");
        }

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            if (!Pattern.matches(PHONE_REGEX, user.getPhone())) {
                result.rejectValue("phone", "error.user",
                        "Please enter a valid phone number (10-15 digits, + optional)");
            } else if (userService.phoneExists(user.getPhone())) {
                result.rejectValue("phone", "error.user",
                        "This phone number is already registered");
            }
        }

        if (!profileImage.isEmpty()) {
            if (profileImage.getSize() > 5 * 1024 * 1024) {
                result.rejectValue("profileImage", "error.user",
                        "Profile image must be less than 5MB");
            }

            String contentType = profileImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.rejectValue("profileImage", "error.user",
                        "Only image files are allowed");
            }
        }

        if (result.hasErrors()) {
            return "pages/register";
        }

        try {
            User savedUser = userService.save(user);

            if (!profileImage.isEmpty()) {
                UserImage userImage = userImageService.saveImage(
                        savedUser.getId(),
                        profileImage
                );
                savedUser.setProfileImageId(userImage.getId());
                userService.save(savedUser);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            return "redirect:/login";
        } catch (IOException e) {
            result.rejectValue("profileImage", "error.user",
                    "Error processing profile image");
            return "pages/register";
        }
    }

    @GetMapping("/user-image/{userId}")
    public ResponseEntity<byte[]> getUserImage(@PathVariable String userId) {
        Optional<UserImage> userImageOpt = userImageService.getImageByUserId(userId);

        return userImageOpt.map(userImage -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(userImage.getContentType()))
                        .body(userImage.getImageData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}