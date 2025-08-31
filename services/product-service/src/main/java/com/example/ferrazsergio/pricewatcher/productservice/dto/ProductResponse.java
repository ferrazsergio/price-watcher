package com.example.ferrazsergio.pricewatcher.productservice.dto;

import com.example.ferrazsergio.pricewatcher.productservice.model.Product;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for products
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        String url,
        BigDecimal targetPrice,
        BigDecimal currentPrice,
        Product.ProductStatus status,
        Product.ProductCategory category,
        Product.SupportedStore store,
        Long userId,
        String imageUrl,
        String brand,
        String model,
        String selector,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lastCheckedAt,
        
        String lastError,
        boolean active
) {
}