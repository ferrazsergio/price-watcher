package com.example.ferrazsergio.pricewatcher.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for price trend analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceTrendResponse {

    private Long productId;
    private String productName;
    private String productUrl;
    private BigDecimal currentPrice;
    private BigDecimal targetPrice;
    private LocalDate analysisStartDate;
    private LocalDate analysisEndDate;
    private Integer totalDays;
    
    // Trend analysis
    private String overallTrend; // BULLISH, BEARISH, SIDEWAYS
    private BigDecimal totalPriceChange;
    private BigDecimal totalPriceChangePercent;
    private BigDecimal averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal volatility;
    
    // Predictions
    private BigDecimal predictedPrice7Days;
    private BigDecimal predictedPrice30Days;
    private String recommendation; // BUY, WAIT, MONITOR
    private Integer confidence; // 0-100
    
    // Historical data
    private List<PriceAnalyticsResponse> dailyAnalytics;
    
    // Statistics
    private Integer daysAboveAverage;
    private Integer daysBelowAverage;
    private Integer volatileDays;
    private BigDecimal bestPrice;
    private BigDecimal worstPrice;
    private LocalDate bestPriceDate;
    private LocalDate worstPriceDate;
}