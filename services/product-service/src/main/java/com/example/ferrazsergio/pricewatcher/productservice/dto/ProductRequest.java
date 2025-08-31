package com.example.ferrazsergio.pricewatcher.productservice.dto;

import com.example.ferrazsergio.pricewatcher.productservice.model.Product;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating products
 */
public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotBlank(message = "Product URL is required")
        @Size(max = 2000, message = "URL must not exceed 2000 characters")
        String url,

        @NotNull(message = "Target price is required")
        @DecimalMin(value = "0.01", message = "Target price must be greater than 0")
        @DecimalMax(value = "999999.99", message = "Target price must not exceed 999999.99")
        BigDecimal targetPrice,

        Product.ProductCategory category,
        
        String imageUrl,
        
        String brand,
        
        String model,
        
        String selector
) {
}