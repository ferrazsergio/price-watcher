package com.example.ferrazsergio.pricewatcher.analytics.service;

import com.example.ferrazsergio.pricewatcher.analytics.dto.PriceAnalyticsResponse;
import com.example.ferrazsergio.pricewatcher.analytics.dto.PriceTrendResponse;
import com.example.ferrazsergio.pricewatcher.analytics.model.PriceAnalytics;
import com.example.ferrazsergio.pricewatcher.analytics.repository.PriceAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for processing and analyzing price data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final PriceAnalyticsRepository analyticsRepository;
    private final WebClient webClient;

    private static final String PRICE_MONITOR_SERVICE_URL = "http://localhost:8083";
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082";

    /**
     * Scheduled job to process daily analytics
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
    public void processDailyAnalytics() {
        log.info("Starting daily analytics processing");
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Long> productIds = getProductsWithPriceHistory();
        
        for (Long productId : productIds) {
            try {
                processDailyAnalyticsForProduct(productId, yesterday);
            } catch (Exception e) {
                log.error("Error processing analytics for product {}: {}", productId, e.getMessage(), e);
            }
        }
        
        log.info("Completed daily analytics processing for {} products", productIds.size());
    }

    /**
     * Process daily analytics for a specific product
     */
    @Transactional
    public PriceAnalytics processDailyAnalyticsForProduct(Long productId, LocalDate date) {
        log.debug("Processing analytics for product {} on date {}", productId, date);

        // Check if analytics already exist for this date
        if (analyticsRepository.existsByProductIdAndDate(productId, date)) {
            log.debug("Analytics already exist for product {} on date {}", productId, date);
            return analyticsRepository.findByProductIdAndDate(productId, date).orElse(null);
        }

        // Fetch price history for the date
        List<Map<String, Object>> priceHistory = fetchPriceHistoryForDate(productId, date);
        
        if (priceHistory.isEmpty()) {
            log.debug("No price history found for product {} on date {}", productId, date);
            return null;
        }

        // Calculate analytics
        PriceAnalytics analytics = calculateDailyAnalytics(productId, date, priceHistory);
        
        // Calculate price change from previous day
        Optional<PriceAnalytics> previousDay = analyticsRepository
                .findFirstByProductIdOrderByDateDesc(productId);
        
        if (previousDay.isPresent()) {
            BigDecimal previousPrice = previousDay.get().getClosePrice();
            BigDecimal currentPrice = analytics.getClosePrice();
            
            BigDecimal priceChange = currentPrice.subtract(previousPrice);
            BigDecimal priceChangePercent = priceChange
                    .divide(previousPrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            
            analytics.setPriceChangeAmount(priceChange);
            analytics.setPriceChangePercent(priceChangePercent);
        }

        analytics.setUpdatedAt(LocalDateTime.now());
        return analyticsRepository.save(analytics);
    }

    /**
     * Get price analytics for a product over a date range
     */
    public List<PriceAnalyticsResponse> getProductAnalytics(Long productId, LocalDate startDate, LocalDate endDate) {
        List<PriceAnalytics> analytics = analyticsRepository
                .findByProductIdAndDateBetweenOrderByDateAsc(productId, startDate, endDate);
        
        return analytics.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get price analytics with pagination
     */
    public Page<PriceAnalyticsResponse> getProductAnalytics(Long productId, LocalDate startDate, 
                                                           LocalDate endDate, Pageable pageable) {
        Page<PriceAnalytics> analytics = analyticsRepository
                .findByProductIdAndDateBetweenOrderByDateDesc(productId, startDate, endDate, pageable);
        
        return analytics.map(this::mapToResponse);
    }

    /**
     * Generate comprehensive price trend analysis
     */
    public PriceTrendResponse generatePriceTrend(Long productId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating price trend for product {} from {} to {}", productId, startDate, endDate);

        List<PriceAnalytics> analytics = analyticsRepository
                .findByProductIdAndDateBetweenOrderByDateAsc(productId, startDate, endDate);

        if (analytics.isEmpty()) {
            return null;
        }

        // Fetch product information
        Map<String, Object> productInfo = fetchProductInfo(productId);
        
        PriceTrendResponse.PriceTrendResponseBuilder builder = PriceTrendResponse.builder()
                .productId(productId)
                .productName((String) productInfo.get("name"))
                .productUrl((String) productInfo.get("url"))
                .currentPrice(productInfo.get("currentPrice") != null ? 
                    new BigDecimal(productInfo.get("currentPrice").toString()) : null)
                .targetPrice(productInfo.get("targetPrice") != null ? 
                    new BigDecimal(productInfo.get("targetPrice").toString()) : null)
                .analysisStartDate(startDate)
                .analysisEndDate(endDate)
                .totalDays(analytics.size());

        // Calculate trend statistics
        calculateTrendStatistics(builder, analytics);
        
        // Add historical data
        List<PriceAnalyticsResponse> dailyAnalytics = analytics.stream()
                .map(this::mapToResponse)
                .toList();
        builder.dailyAnalytics(dailyAnalytics);

        return builder.build();
    }

    /**
     * Calculate trend statistics
     */
    private void calculateTrendStatistics(PriceTrendResponse.PriceTrendResponseBuilder builder, 
                                        List<PriceAnalytics> analytics) {
        
        if (analytics.isEmpty()) return;

        PriceAnalytics first = analytics.get(0);
        PriceAnalytics last = analytics.get(analytics.size() - 1);

        // Price change calculations
        BigDecimal totalChange = last.getClosePrice().subtract(first.getOpenPrice());
        BigDecimal totalChangePercent = totalChange
                .divide(first.getOpenPrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        // Statistical calculations
        BigDecimal sumPrices = analytics.stream()
                .map(PriceAnalytics::getAvgPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgPrice = sumPrices.divide(new BigDecimal(analytics.size()), 2, RoundingMode.HALF_UP);

        BigDecimal minPrice = analytics.stream()
                .map(PriceAnalytics::getMinPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = analytics.stream()
                .map(PriceAnalytics::getMaxPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // Volatility calculation (standard deviation / mean)
        BigDecimal volatility = calculateVolatility(analytics, avgPrice);

        // Count statistics
        long daysAboveAverage = analytics.stream()
                .filter(a -> a.getAvgPrice().compareTo(avgPrice) > 0)
                .count();
        long daysBelowAverage = analytics.size() - daysAboveAverage;
        long volatileDays = analytics.stream()
                .filter(PriceAnalytics::isPriceVolatile)
                .count();

        // Find best and worst prices
        Optional<PriceAnalytics> bestPriceDay = analytics.stream()
                .filter(a -> a.getMinPrice().equals(minPrice))
                .findFirst();
        Optional<PriceAnalytics> worstPriceDay = analytics.stream()
                .filter(a -> a.getMaxPrice().equals(maxPrice))
                .findFirst();

        // Determine overall trend
        String overallTrend = determineOverallTrend(totalChangePercent, volatility);
        
        // Generate predictions (simple linear regression)
        BigDecimal predicted7Days = generateSimplePrediction(analytics, 7);
        BigDecimal predicted30Days = generateSimplePrediction(analytics, 30);
        
        // Generate recommendation
        String recommendation = generateRecommendation(last.getClosePrice(), avgPrice, overallTrend, volatility);
        int confidence = calculateConfidence(analytics.size(), volatility);

        builder
                .overallTrend(overallTrend)
                .totalPriceChange(totalChange)
                .totalPriceChangePercent(totalChangePercent)
                .averagePrice(avgPrice)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .volatility(volatility)
                .predictedPrice7Days(predicted7Days)
                .predictedPrice30Days(predicted30Days)
                .recommendation(recommendation)
                .confidence(confidence)
                .daysAboveAverage((int) daysAboveAverage)
                .daysBelowAverage((int) daysBelowAverage)
                .volatileDays((int) volatileDays)
                .bestPrice(minPrice)
                .worstPrice(maxPrice)
                .bestPriceDate(bestPriceDay.map(PriceAnalytics::getDate).orElse(null))
                .worstPriceDate(worstPriceDay.map(PriceAnalytics::getDate).orElse(null));
    }

    /**
     * Calculate volatility (coefficient of variation)
     */
    private BigDecimal calculateVolatility(List<PriceAnalytics> analytics, BigDecimal mean) {
        if (analytics.size() < 2) return BigDecimal.ZERO;

        BigDecimal sumSquaredDiffs = analytics.stream()
                .map(a -> a.getAvgPrice().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variance = sumSquaredDiffs.divide(new BigDecimal(analytics.size()), 4, RoundingMode.HALF_UP);
        BigDecimal stdDev = sqrt(variance);
        
        return stdDev.divide(mean, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * Simple square root calculation using Newton's method
     */
    private BigDecimal sqrt(BigDecimal value) {
        if (value.equals(BigDecimal.ZERO)) return BigDecimal.ZERO;
        
        BigDecimal x = value.divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        BigDecimal prev;
        
        do {
            prev = x;
            x = x.add(value.divide(x, 4, RoundingMode.HALF_UP)).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        } while (x.subtract(prev).abs().compareTo(new BigDecimal("0.0001")) > 0);
        
        return x;
    }

    /**
     * Determine overall trend
     */
    private String determineOverallTrend(BigDecimal priceChangePercent, BigDecimal volatility) {
        BigDecimal threshold = new BigDecimal("5"); // 5% threshold
        
        if (priceChangePercent.compareTo(threshold) > 0) {
            return "BULLISH";
        } else if (priceChangePercent.compareTo(threshold.negate()) < 0) {
            return "BEARISH";
        } else {
            return "SIDEWAYS";
        }
    }

    /**
     * Generate simple prediction based on trend
     */
    private BigDecimal generateSimplePrediction(List<PriceAnalytics> analytics, int daysAhead) {
        if (analytics.size() < 2) {
            return analytics.get(analytics.size() - 1).getClosePrice();
        }

        // Simple linear regression
        int n = analytics.size();
        BigDecimal sumX = BigDecimal.ZERO;
        BigDecimal sumY = BigDecimal.ZERO;
        BigDecimal sumXY = BigDecimal.ZERO;
        BigDecimal sumX2 = BigDecimal.ZERO;

        for (int i = 0; i < n; i++) {
            BigDecimal x = new BigDecimal(i);
            BigDecimal y = analytics.get(i).getClosePrice();
            
            sumX = sumX.add(x);
            sumY = sumY.add(y);
            sumXY = sumXY.add(x.multiply(y));
            sumX2 = sumX2.add(x.multiply(x));
        }

        BigDecimal nBig = new BigDecimal(n);
        BigDecimal slope = (nBig.multiply(sumXY).subtract(sumX.multiply(sumY)))
                .divide(nBig.multiply(sumX2).subtract(sumX.multiply(sumX)), 4, RoundingMode.HALF_UP);
        
        BigDecimal intercept = (sumY.subtract(slope.multiply(sumX)))
                .divide(nBig, 4, RoundingMode.HALF_UP);

        BigDecimal futureX = new BigDecimal(n + daysAhead - 1);
        return slope.multiply(futureX).add(intercept);
    }

    /**
     * Generate recommendation
     */
    private String generateRecommendation(BigDecimal currentPrice, BigDecimal avgPrice, 
                                        String trend, BigDecimal volatility) {
        
        BigDecimal lowVolatilityThreshold = new BigDecimal("10"); // 10%
        BigDecimal priceThreshold = new BigDecimal("0.05"); // 5%
        
        BigDecimal priceDiffPercent = currentPrice.subtract(avgPrice)
                .divide(avgPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).abs();

        if ("BEARISH".equals(trend) && volatility.compareTo(lowVolatilityThreshold) < 0) {
            return "BUY";
        } else if ("BULLISH".equals(trend) && priceDiffPercent.compareTo(priceThreshold.multiply(new BigDecimal("100"))) > 0) {
            return "WAIT";
        } else {
            return "MONITOR";
        }
    }

    /**
     * Calculate confidence level
     */
    private int calculateConfidence(int dataPoints, BigDecimal volatility) {
        int baseConfidence = Math.min(dataPoints * 2, 80); // More data = higher confidence
        int volatilityPenalty = volatility.intValue(); // Higher volatility = lower confidence
        return Math.max(20, Math.min(100, baseConfidence - volatilityPenalty));
    }

    // Continue in next part...

    /**
     * Calculate daily analytics from price history data
     */
    private PriceAnalytics calculateDailyAnalytics(Long productId, LocalDate date, 
                                                 List<Map<String, Object>> priceHistory) {
        
        List<BigDecimal> prices = priceHistory.stream()
                .map(entry -> new BigDecimal(entry.get("price").toString()))
                .toList();

        BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        BigDecimal avgPrice = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(prices.size()), 2, RoundingMode.HALF_UP);

        BigDecimal openPrice = prices.get(0); // First price of the day
        BigDecimal closePrice = prices.get(prices.size() - 1); // Last price of the day

        boolean isWorkingDay = !isWeekend(date);

        return PriceAnalytics.builder()
                .productId(productId)
                .date(date)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .avgPrice(avgPrice)
                .openPrice(openPrice)
                .closePrice(closePrice)
                .checkCount(prices.size())
                .isWorkingDay(isWorkingDay)
                .build();
    }

    /**
     * Check if date is weekend
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Fetch products with price history
     */
    private List<Long> getProductsWithPriceHistory() {
        try {
            Mono<List> response = webClient.get()
                    .uri(PRICE_MONITOR_SERVICE_URL + "/api/price-monitor/changes?hours=48")
                    .retrieve()
                    .bodyToMono(List.class);

            return response.block();
        } catch (Exception e) {
            log.error("Error fetching products with price history", e);
            return List.of();
        }
    }

    /**
     * Fetch price history for a specific date
     */
    private List<Map<String, Object>> fetchPriceHistoryForDate(Long productId, LocalDate date) {
        try {
            Mono<List> response = webClient.get()
                    .uri(PRICE_MONITOR_SERVICE_URL + "/api/price-monitor/history/" + productId + "?days=1")
                    .retrieve()
                    .bodyToMono(List.class);

            List<Map<String, Object>> history = response.block();
            
            // Filter for the specific date
            return history.stream()
                    .filter(entry -> {
                        String checkedAt = (String) entry.get("checkedAt");
                        return checkedAt != null && checkedAt.startsWith(date.toString());
                    })
                    .map(entry -> (Map<String, Object>) entry)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching price history for product {} on date {}", productId, date, e);
            return List.of();
        }
    }

    /**
     * Fetch product information
     */
    private Map<String, Object> fetchProductInfo(Long productId) {
        try {
            Mono<Map> response = webClient.get()
                    .uri(PRODUCT_SERVICE_URL + "/api/products/" + productId)
                    .retrieve()
                    .bodyToMono(Map.class);

            Map<String, Object> productResponse = response.block();
            return (Map<String, Object>) productResponse.get("data");
        } catch (Exception e) {
            log.error("Error fetching product info for product {}", productId, e);
            return Map.of("name", "Unknown Product", "url", "");
        }
    }

    /**
     * Map analytics entity to response DTO
     */
    private PriceAnalyticsResponse mapToResponse(PriceAnalytics analytics) {
        String trend = "STABLE";
        if (analytics.isPriceIncreasing()) {
            trend = "INCREASING";
        } else if (analytics.isPriceDecreasing()) {
            trend = "DECREASING";
        }

        return PriceAnalyticsResponse.builder()
                .productId(analytics.getProductId())
                .date(analytics.getDate())
                .minPrice(analytics.getMinPrice())
                .maxPrice(analytics.getMaxPrice())
                .avgPrice(analytics.getAvgPrice())
                .openPrice(analytics.getOpenPrice())
                .closePrice(analytics.getClosePrice())
                .checkCount(analytics.getCheckCount())
                .priceChangeAmount(analytics.getPriceChangeAmount())
                .priceChangePercent(analytics.getPriceChangePercent())
                .isWorkingDay(analytics.isWorkingDay())
                .createdAt(analytics.getCreatedAt())
                .priceRange(analytics.getPriceRange())
                .isVolatile(analytics.isPriceVolatile())
                .trend(trend)
                .build();
    }

    /**
     * Cleanup old analytics data
     */
    @Scheduled(cron = "0 0 2 1 * ?") // Run at 2 AM on the 1st of every month
    @Transactional
    public void cleanupOldAnalytics() {
        LocalDate cutoff = LocalDate.now().minusMonths(6); // Keep 6 months of data
        log.info("Cleaning up analytics data older than {}", cutoff);
        
        analyticsRepository.deleteByDateBefore(cutoff);
        
        log.info("Completed cleanup of old analytics data");
    }

    /**
     * Get summary statistics for a user's products
     */
    public Map<String, Object> getUserAnalyticsSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        // This would require integration with user service to get user's products
        // For now, return a placeholder
        return Map.of(
                "message", "User analytics summary not yet implemented",
                "userId", userId,
                "startDate", startDate,
                "endDate", endDate
        );
    }
}