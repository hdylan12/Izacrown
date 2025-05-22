package com.web.tech.controller;

import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        if (userService.validateEmail(email)) {
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", email);
            model.addAttribute("successMessage", "Email verified. Please enter your new password.");
        } else {
            model.addAttribute("errorMessage", "Email not found.");
        }
        return "forgot-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "forgot-password";
        }
        try {
            userService.resetPassword(email, newPassword);
            return "redirect:/login?successMessage=Password+reset+successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("showResetForm", true);
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", "User not found.");
            return "forgot-password";
        }
    }
}