package com.web.tech.controller;

import com.web.tech.model.Products;
import com.web.tech.model.User;
import com.web.tech.service.NotificationService;
import com.web.tech.service.ProductImageService;
import com.web.tech.service.ProductService;
import com.web.tech.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public ProductController(ProductService productService, ProductImageService productImageService,
                             UserService userService, NotificationService notificationService) {
        this.productService = productService;
        this.productImageService = productImageService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Products());
        model.addAttribute("pageTitle", "Add Artwork");
        return "admin/products/add";
    }

    @PostMapping("/products/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") Products product,
            BindingResult result,
            @RequestParam("image") MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", product.getId() == null ? "Add Artwork" : "Edit Artwork");
            return "admin/products/add";
        }

        if (image.isEmpty() && product.getId() == null) {
            model.addAttribute("imageError", "Artwork image is required");
            model.addAttribute("pageTitle", "Add Artwork");
            return "admin/products/add";
        }

        try {
            productService.saveProduct(product, image.isEmpty() ? null : image);
            redirectAttributes.addFlashAttribute("successMessage",
                    product.getId() == null ? "Artwork added successfully!" : "Artwork updated successfully!");
            return "redirect:/admin/artworks";
        } catch (Exception e) {
            System.err.println("Error saving product: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving artwork: " + e.getMessage());
            return product.getId() == null ? "redirect:/admin/products/add" : "redirect:/admin/products/edit/" + product.getId();
        }
    }

    @GetMapping("/artworks")
    public String showArtworks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            Model model) {

        Page<Products> artworkPage;

        if (name != null || minPrice != null || maxPrice != null) {
            try {
                artworkPage = productService.searchProducts(name, minPrice, maxPrice, pageable);
            } catch (IllegalArgumentException e) {
                model.addAttribute("searchError", e.getMessage());
                artworkPage = productService.getAllProducts(pageable);
            }
        } else {
            artworkPage = productService.getAllProducts(pageable);
        }

        model.addAttribute("artworks", artworkPage.getContent());
        model.addAttribute("page", artworkPage);
        model.addAttribute("pageTitle", "Artworks");
        model.addAttribute("currentPage", "artworks");
        return "admin/products/artworks";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(
            @PathVariable("id") String id,
            RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName().toLowerCase());
            notificationService.createNotification(
                    "Artwork Deleted",
                    "Artwork with ID " + id + " was successfully deleted.",
                    "SUCCESS",
                    user
            );
            redirectAttributes.addFlashAttribute("successMessage", "Artwork deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error deleting product ID: " + id + " - " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error deleting artwork: " + e.getMessage());
        }
        return "redirect:/admin/artworks";
    }

    @GetMapping("/products/edit/{id}")
    public String editArtworkForm(@PathVariable String id, Model model) {
        Optional<Products> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/admin/artworks?error=Artwork not found";
        }

        model.addAttribute("product", productOpt.get());
        model.addAttribute("pageTitle", "Edit Artwork");
        return "admin/products/edit";
    }

    @PostMapping("/products/edit/{id}")
    public String updateArtwork(
            @PathVariable String id,
            @Valid @ModelAttribute("product") Products product,
            BindingResult bindingResult,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Artwork");
            return "admin/products/edit";
        }

        Optional<Products> existingProductOpt = productService.getProductById(id);
        if (existingProductOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Artwork not found");
            return "redirect:/admin/artworks";
        }

        Products existingProduct = existingProductOpt.get();
        existingProduct.setName(product.getName());
        existingProduct.setArtist(product.getArtist());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setMedium(product.getMedium());
        existingProduct.setDimensions(product.getDimensions());
        existingProduct.setYear(product.getYear());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setCategory(product.getCategory());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                if (!imageFile.getContentType().startsWith("image/")) {
                    model.addAttribute("imageError", "Only image files are allowed");
                    model.addAttribute("pageTitle", "Edit Artwork");
                    return "admin/products/edit";
                }

                if (imageFile.getSize() > 5 * 1024 * 1024) {
                    model.addAttribute("imageError", "Image size exceeds 5MB limit");
                    model.addAttribute("pageTitle", "Edit Artwork");
                    return "admin/products/edit";
                }

                productImageService.deleteImage(id);
                productImageService.saveImage(id, imageFile);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload image: " + e.getMessage());
                return "redirect:/admin/products/edit/" + id;
            }
        }

        try {
            productService.saveProduct(existingProduct, null);
            redirectAttributes.addFlashAttribute("successMessage", "Artwork updated successfully!");
            return "redirect:/admin/artworks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating artwork: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }
}