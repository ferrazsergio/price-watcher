package com.example.ferrazsergio.pricewatcher.pricemonitor.controller;

import com.example.ferrazsergio.pricewatcher.pricemonitor.model.PriceHistory;
import com.example.ferrazsergio.pricewatcher.pricemonitor.service.PriceMonitoringService;
import com.example.ferrazsergio.pricewatcher.pricemonitor.service.ProductInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for price monitoring operations
 */
@RestController
@RequestMapping("/api/price-monitor")
@RequiredArgsConstructor
@Slf4j
public class PriceMonitorController {

    private final PriceMonitoringService priceMonitoringService;

    /**
     * Manually trigger price monitoring for a specific product
     */
    @PostMapping("/monitor/{productId}")
    public ResponseEntity<PriceHistory> monitorProduct(@PathVariable Long productId,
                                                      @RequestBody ProductInfo productInfo) {
        log.info("Manual price monitoring triggered for product: {}", productId);
        
        try {
            PriceHistory result = priceMonitoringService.monitorProduct(productInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error monitoring product {}: {}", productId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get price history for a product
     */
    @GetMapping("/history/{productId}")
    public ResponseEntity<List<PriceHistory>> getPriceHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int days) {
        
        List<PriceHistory> history = priceMonitoringService.getPriceHistory(productId, days);
        return ResponseEntity.ok(history);
    }

    /**
     * Get latest price for a product
     */
    @GetMapping("/latest/{productId}")
    public ResponseEntity<PriceHistory> getLatestPrice(@PathVariable Long productId) {
        Optional<PriceHistory> latestPrice = priceMonitoringService.getLatestPrice(productId);
        
        if (latestPrice.isPresent()) {
            return ResponseEntity.ok(latestPrice.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get products with recent price changes
     */
    @GetMapping("/changes")
    public ResponseEntity<List<Long>> getRecentPriceChanges(
            @RequestParam(defaultValue = "24") int hours) {
        
        List<Long> changedProducts = priceMonitoringService.getProductsWithRecentPriceChanges(hours);
        return ResponseEntity.ok(changedProducts);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Price Monitor Service is running");
    }

    /**
     * Trigger manual monitoring for all products (admin endpoint)
     */
    @PostMapping("/monitor-all")
    public ResponseEntity<String> monitorAllProducts() {
        log.info("Manual monitoring triggered for all products");
        
        try {
            priceMonitoringService.monitorAllProducts();
            return ResponseEntity.ok("Monitoring triggered for all products");
        } catch (Exception e) {
            log.error("Error triggering manual monitoring", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}