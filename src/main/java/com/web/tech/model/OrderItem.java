package com.web.tech.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.math.BigDecimal;

public class OrderItem {

    private String id;

    @DBRef
    private Products product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String productName;

    public OrderItem() {}

    public OrderItem(Products product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
        this.productName = product.getName();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Products getProduct() { return product; }
    public void setProduct(Products product) {
        this.product = product;
        if (product != null) {
            this.productName = product.getName();
        }
    }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(quantity));
    }
}