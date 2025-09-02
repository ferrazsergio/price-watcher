package com.example.ferrazsergio.pricewatcher.analytics.controller;

import com.example.ferrazsergio.pricewatcher.analytics.dto.PriceAnalyticsResponse;
import com.example.ferrazsergio.pricewatcher.analytics.dto.PriceTrendResponse;
import com.example.ferrazsergio.pricewatcher.analytics.model.PriceAnalytics;
import com.example.ferrazsergio.pricewatcher.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for analytics operations
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get price analytics for a product over a date range
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PriceAnalyticsResponse>> getProductAnalytics(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting analytics for product {} from {} to {}", productId, startDate, endDate);
        
        List<PriceAnalyticsResponse> analytics = analyticsService.getProductAnalytics(productId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get price analytics with pagination
     */
    @GetMapping("/product/{productId}/paginated")
    public ResponseEntity<Page<PriceAnalyticsResponse>> getProductAnalyticsPaginated(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        log.info("Getting paginated analytics for product {} from {} to {}", productId, startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PriceAnalyticsResponse> analytics = analyticsService.getProductAnalytics(
                productId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(analytics);
    }

    /**
     * Generate comprehensive price trend analysis
     */
    @GetMapping("/product/{productId}/trend")
    public ResponseEntity<PriceTrendResponse> getProductTrend(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int days) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        log.info("Generating trend analysis for product {} over {} days", productId, days);
        
        PriceTrendResponse trend = analyticsService.generatePriceTrend(productId, startDate, endDate);
        
        if (trend == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(trend);
    }

    /**
     * Generate price trend analysis for custom date range
     */
    @GetMapping("/product/{productId}/trend/custom")
    public ResponseEntity<PriceTrendResponse> getProductTrendCustom(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating custom trend analysis for product {} from {} to {}", productId, startDate, endDate);
        
        PriceTrendResponse trend = analyticsService.generatePriceTrend(productId, startDate, endDate);
        
        if (trend == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(trend);
    }

    /**
     * Manually trigger analytics processing for a specific product and date
     */
    @PostMapping("/process/{productId}")
    public ResponseEntity<PriceAnalytics> processProductAnalytics(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now().minusDays(1);
        }
        
        log.info("Manually processing analytics for product {} on date {}", productId, date);
        
        try {
            PriceAnalytics analytics = analyticsService.processDailyAnalyticsForProduct(productId, date);
            
            if (analytics == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error processing analytics for product {}: {}", productId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Trigger analytics processing for all products (admin endpoint)
     */
    @PostMapping("/process-all")
    public ResponseEntity<String> processAllAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now().minusDays(1);
        }
        
        log.info("Manually triggering analytics processing for all products on date {}", date);
        
        try {
            analyticsService.processDailyAnalytics();
            return ResponseEntity.ok("Analytics processing triggered for all products");
        } catch (Exception e) {
            log.error("Error triggering analytics processing", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get analytics summary for a user
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getUserAnalyticsSummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        log.info("Getting analytics summary for user {} over {} days", userId, days);
        
        Map<String, Object> summary = analyticsService.getUserAnalyticsSummary(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Service is running");
    }

    /**
     * Get analytics statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAnalyticsStats() {
        // This could include statistics about the analytics service
        Map<String, Object> stats = Map.of(
                "service", "Analytics Service",
                "status", "Running",
                "lastProcessed", LocalDate.now().minusDays(1),
                "version", "1.0.0"
        );
        
        return ResponseEntity.ok(stats);
    }
}