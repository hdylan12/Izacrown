package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.service.UserImageService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class ProfileImageController {

    @Autowired
    private UserImageService userImageService;

    @Autowired
    private UserService userService;

    @PostMapping("/admin/upload-profile-image")
    public String uploadProfileImage(@RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to upload an image");
            return "redirect:/login";
        }

        String email = auth.getName().toLowerCase();
        User user = userService.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin";
        }

        try {
            userImageService.updateImage(user.getId(), file);
            redirectAttributes.addFlashAttribute("successMessage", "Profile image updated successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update image: " + e.getMessage());
        }

        return "redirect:/admin";
    }
}