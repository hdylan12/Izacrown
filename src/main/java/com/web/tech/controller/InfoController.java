package com.web.tech.controller;

import com.web.tech.service.CartService;
import com.web.tech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class InfoController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @GetMapping("/about")
    public String about(Model model) {
        return "admin/user/about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return "admin/user/contact";
    }

    @PostMapping("/contact")
    @ResponseBody
    public String submitContactForm(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String message) {
        try {
            if (name.isBlank() || email.isBlank() || message.isBlank()) {
                return "error:All fields are required";
            }
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return "error:Invalid email address";
            }
            return "success:Your message has been sent successfully!";
        } catch (Exception e) {
            return "error:Failed to send message: " + e.getMessage();
        }
    }
}