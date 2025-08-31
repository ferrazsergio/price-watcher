package com.example.ferrazsergio.pricewatcher.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event fired when a price change is detected
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PriceChangeDetectedEvent extends BaseEvent {
    
    private Long productId;
    private String productName;
    private String productUrl;
    private BigDecimal previousPrice;
    private BigDecimal currentPrice;
    private BigDecimal targetPrice;
    private Long userId;
    private String notificationChannel;
    private String phoneNumber;
    
    public PriceChangeDetectedEvent(Long productId, String productName, String productUrl,
                                  BigDecimal previousPrice, BigDecimal currentPrice, BigDecimal targetPrice,
                                  Long userId, String notificationChannel, String phoneNumber) {
        super("PRICE_CHANGE_DETECTED");
        this.productId = productId;
        this.productName = productName;
        this.productUrl = productUrl;
        this.previousPrice = previousPrice;
        this.currentPrice = currentPrice;
        this.targetPrice = targetPrice;
        this.userId = userId;
        this.notificationChannel = notificationChannel;
        this.phoneNumber = phoneNumber;
    }
}