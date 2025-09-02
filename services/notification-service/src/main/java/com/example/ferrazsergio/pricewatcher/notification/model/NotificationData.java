package com.example.ferrazsergio.pricewatcher.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for notification data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationData {
    
    private String type; // EMAIL, SMS, PUSH, TELEGRAM
    private String recipient;
    private String subject;
    private String message;
    private String template;
    
    // Price change specific data
    private Long productId;
    private String productName;
    private String productUrl;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal targetPrice;
    private Long userId;
    private String userEmail;
    private String phoneNumber;
    
    // Additional metadata
    private LocalDateTime createdAt = LocalDateTime.now();
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    private String category = "PRICE_CHANGE"; // PRICE_CHANGE, WELCOME, SYSTEM, etc.
    
    public boolean isPriceTarget() {
        return newPrice != null && targetPrice != null && newPrice.compareTo(targetPrice) <= 0;
    }
    
    public boolean isPriceIncrease() {
        return oldPrice != null && newPrice != null && newPrice.compareTo(oldPrice) > 0;
    }
    
    public boolean isPriceDecrease() {
        return oldPrice != null && newPrice != null && newPrice.compareTo(oldPrice) < 0;
    }
}