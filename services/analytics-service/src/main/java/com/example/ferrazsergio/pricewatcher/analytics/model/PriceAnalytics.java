package com.example.ferrazsergio.pricewatcher.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for storing aggregated price analytics data
 */
@Entity
@Table(name = "price_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal avgPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal openPrice; // First price of the day

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal closePrice; // Last price of the day

    @Column(nullable = false)
    private Integer checkCount; // Number of price checks on this day

    @Column
    private BigDecimal priceChangeAmount; // Change from previous day

    @Column
    private BigDecimal priceChangePercent; // Percentage change from previous day

    @Column(nullable = false)
    private boolean isWorkingDay;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Helper methods
    public BigDecimal getPriceRange() {
        return maxPrice.subtract(minPrice);
    }

    public boolean isPriceVolatile() {
        if (avgPrice.compareTo(BigDecimal.ZERO) == 0) return false;
        BigDecimal volatilityThreshold = new BigDecimal("0.1"); // 10%
        BigDecimal volatility = getPriceRange().divide(avgPrice, 4, java.math.RoundingMode.HALF_UP);
        return volatility.compareTo(volatilityThreshold) > 0;
    }

    public boolean isPriceIncreasing() {
        return priceChangeAmount != null && priceChangeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPriceDecreasing() {
        return priceChangeAmount != null && priceChangeAmount.compareTo(BigDecimal.ZERO) < 0;
    }
}