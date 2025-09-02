package com.example.ferrazsergio.pricewatcher.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for price analytics response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAnalyticsResponse {

    private Long productId;
    private String productName;
    private LocalDate date;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal avgPrice;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private Integer checkCount;
    private BigDecimal priceChangeAmount;
    private BigDecimal priceChangePercent;
    private boolean isWorkingDay;
    private LocalDateTime createdAt;
    
    // Derived fields
    private BigDecimal priceRange;
    private boolean isVolatile;
    private String trend; // INCREASING, DECREASING, STABLE
}