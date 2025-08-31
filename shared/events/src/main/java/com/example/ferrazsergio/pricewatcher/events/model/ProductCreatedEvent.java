package com.example.ferrazsergio.pricewatcher.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a product is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {
    private Long productId;
    private String productName;
    private String productUrl;
    private BigDecimal targetPrice;
    private Long userId;
    private String store;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ProductCreatedEvent(Long productId, String productName, String productUrl, 
                             BigDecimal targetPrice, Long userId, String store) {
        this.productId = productId;
        this.productName = productName;
        this.productUrl = productUrl;
        this.targetPrice = targetPrice;
        this.userId = userId;
        this.store = store;
        this.timestamp = LocalDateTime.now();
    }
}