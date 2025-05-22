package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/clients")
public class ClientDisplayController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"", "/"})
    public String showClients(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name-asc") String sort,
            Model model) {

        String sortField;
        Sort.Direction direction;
        switch (sort) {
            case "name-desc":
                sortField = "lastName";
                direction = Sort.Direction.DESC;
                break;
            case "email-asc":
                sortField = "email";
                direction = Sort.Direction.ASC;
                break;
            case "email-desc":
                sortField = "email";
                direction = Sort.Direction.DESC;
                break;
            case "createdAt-asc":
                sortField = "createdAt";
                direction = Sort.Direction.ASC;
                break;
            case "createdAt-desc":
                sortField = "createdAt";
                direction = Sort.Direction.DESC;
                break;
            default:
                sortField = "lastName";
                direction = Sort.Direction.ASC;
                break;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<User> userPage = query.isEmpty()
                ? userService.getAllUsers(pageable)
                : userService.findByNameOrEmail(query, pageable);

        model.addAttribute("page", userPage);
        model.addAttribute("query", query);
        model.addAttribute("sort", sort);
        model.addAttribute("pageTitle", "Client Management");
        model.addAttribute("currentPage", "clients");
        model.addAttribute("editUser", new User());
        return "admin/products/clients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("id") String id,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name-asc") String sort,
            Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        model.addAttribute("editUser", user);
        model.addAttribute("query", query);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("pageTitle", "Edit Client");
        model.addAttribute("currentPage", "clients");
        return "admin/products/edit-client";
    }

    @PostMapping("/editUser")
    public String editClient(
            @Valid @ModelAttribute("editUser") User user,
            BindingResult result,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name-asc") String sort,
            Model model) {

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("query", query);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("pageTitle", "Edit Client");
            model.addAttribute("currentPage", "clients");
            return "admin/products/edit-client";
        }

        try {
            System.out.println("Updating user: " + user);
            userService.updateUser(user);
            System.out.println("User updated, redirecting to /admin/clients");
        } catch (Exception e) {
            System.err.println("Update failed: " + e.getMessage());
            model.addAttribute("errorMessage", "Failed to update user: " + e.getMessage());
            model.addAttribute("query", query);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("pageTitle", "Edit Client");
            model.addAttribute("currentPage", "clients");
            return "admin/products/edit-client";
        }

        return "redirect:/admin/clients?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&page=" + page + "&size=" + size + "&sort=" + sort;
    }
}