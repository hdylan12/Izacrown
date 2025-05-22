package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.service.UserImageService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final UserImageService userImageService;

    @Autowired
    public AccountController(UserService userService, UserImageService userImageService) {
        this.userService = userService;
        this.userImageService = userImageService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String accountPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);

        String profilePicture = userImageService.getProfileImageBase64(user.getId());
        String contentType = userImageService.getProfileImageContentType(user.getId());
        model.addAttribute("profilePicture",
                profilePicture != null ? "data:" + contentType + ";base64," + profilePicture : "/images/default-avatar.jpg");

        return "admin/user/profile";
    }

    @PostMapping("/upload-profile-image")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String uploadProfileImage(@RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName());
            userImageService.updateImage(user.getId(), file);
            redirectAttributes.addFlashAttribute("successMessage", "Profile picture updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload profile picture: " + e.getMessage());
        }
        return "redirect:/account";
    }

    @PostMapping("/update-info")
    public String updateUserInfo(@ModelAttribute User updatedUser,
                                 RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());

            currentUser.setFirstName(updatedUser.getFirstName());
            currentUser.setLastName(updatedUser.getLastName());
            currentUser.setPhone(updatedUser.getPhone());
            currentUser.setAddress(updatedUser.getAddress());

            userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Profile information updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/account";
    }
}