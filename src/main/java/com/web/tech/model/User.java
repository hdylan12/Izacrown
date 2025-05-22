package com.web.tech.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "clients")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id; // MongoDB ObjectId as String

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String confirmPassword;

    private String profileImageId;

    private String role = "USER"; // Default role

    private String address;

    private String city;

    private String zipCode;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Indexed(unique = true)
    private String phone;

    // Note: Orders list should be handled carefully for serialization
    // Consider storing only order IDs instead of full Order objects
    private List<String> orderIds = new ArrayList<>(); // Changed from Orders to String IDs

    private Integer orderCount = 0;

    private BigDecimal totalSpent = BigDecimal.ZERO;

    // Default constructor
    public User() {}

    // Constructor for essential fields
    public User(String firstName, String lastName, String email, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email != null ? email.toLowerCase().trim() : null;
        this.password = password;
        this.role = role != null ? role : "USER";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getProfileImageId() {
        return profileImageId;
    }

    public void setProfileImageId(String profileImageId) {
        this.profileImageId = profileImageId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds != null ? orderIds : new ArrayList<>();
    }

    // Backward compatibility - you may need to update references to this method
    @Deprecated
    public List<Orders> getOrders() {
        // Return empty list or implement logic to fetch Orders by IDs
        return new ArrayList<>();
    }

    @Deprecated
    public void setOrders(List<Orders> orders) {
        // Convert Orders to IDs if needed
        // This method should be updated based on your Orders entity structure
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getProfilePicture() {
        return profileImageId;
    }

    public void setProfilePicture(String profilePicture) {
        this.profileImageId = profilePicture;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", phone='" + phone + '\'' +
                ", orderCount=" + orderCount +
                ", totalSpent=" + totalSpent +
                '}';
    }
}