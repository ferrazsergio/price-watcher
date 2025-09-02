package com.example.ferrazsergio.pricewatcher.pricemonitor.service;

import com.example.ferrazsergio.pricewatcher.events.model.PriceChangeDetectedEvent;
import com.example.ferrazsergio.pricewatcher.pricemonitor.model.PriceHistory;
import com.example.ferrazsergio.pricewatcher.pricemonitor.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core service for monitoring product prices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceMonitoringService {

    private final WebScrapingService webScrapingService;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;

    @Value("${price-monitor.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    private static final String PRICE_WATCHER_EXCHANGE = "price-watcher-exchange";
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082";

    /**
     * Scheduled method to monitor all active products
     */
    @Scheduled(fixedDelayString = "${price-monitor.scheduler.fixed-delay:300000}",
               initialDelayString = "${price-monitor.scheduler.initial-delay:60000}")
    public void monitorAllProducts() {
        if (!schedulerEnabled) {
            log.debug("Price monitoring scheduler is disabled");
            return;
        }

        log.info("Starting scheduled price monitoring check");
        
        try {
            List<ProductInfo> activeProducts = fetchActiveProducts();
            log.info("Found {} active products to monitor", activeProducts.size());

            for (ProductInfo product : activeProducts) {
                monitorProductAsync(product);
            }
        } catch (Exception e) {
            log.error("Error during scheduled price monitoring", e);
        }
    }

    /**
     * Monitor a specific product asynchronously
     */
    @Async
    public void monitorProductAsync(ProductInfo product) {
        try {
            monitorProduct(product);
        } catch (Exception e) {
            log.error("Error monitoring product {}: {}", product.getId(), e.getMessage(), e);
        }
    }

    /**
     * Monitor a specific product and detect price changes
     */
    @Transactional
    public PriceHistory monitorProduct(ProductInfo product) {
        log.debug("Monitoring product: {} - {}", product.getId(), product.getName());

        PriceHistory priceHistory = PriceHistory.builder()
                .productId(product.getId())
                .source(product.getStore())
                .checkedAt(LocalDateTime.now())
                .build();

        try {
            // Get previous price
            Optional<PriceHistory> lastHistory = priceHistoryRepository
                    .findFirstByProductIdOrderByCheckedAtDesc(product.getId());
            
            BigDecimal previousPrice = lastHistory.map(PriceHistory::getPrice).orElse(null);
            priceHistory.setPreviousPrice(previousPrice);

            // Scrape current price
            BigDecimal currentPrice = webScrapingService.scrapePrice(
                    product.getUrl(), 
                    product.getSelector(), 
                    product.getStore()
            );

            priceHistory.setPrice(currentPrice);
            priceHistory.setAvailable(true);

            // Update product's current price
            updateProductCurrentPrice(product.getId(), currentPrice);

            // Check for price changes and send notifications
            if (previousPrice != null && currentPrice.compareTo(previousPrice) != 0) {
                log.info("Price change detected for product {}: {} -> {}", 
                        product.getId(), previousPrice, currentPrice);
                
                publishPriceChangeEvent(product, previousPrice, currentPrice);
            }

            // Check if target price is achieved
            if (product.getTargetPrice() != null && 
                currentPrice.compareTo(product.getTargetPrice()) <= 0) {
                log.info("Target price achieved for product {}: current={}, target={}", 
                        product.getId(), currentPrice, product.getTargetPrice());
                
                updateProductStatus(product.getId(), "PRICE_ACHIEVED");
                publishPriceChangeEvent(product, previousPrice, currentPrice);
            }

        } catch (Exception e) {
            log.error("Error monitoring product {}: {}", product.getId(), e.getMessage());
            priceHistory.setAvailable(false);
            priceHistory.setError(e.getMessage());
            
            // Update product with error
            updateProductError(product.getId(), e.getMessage());
        }

        return priceHistoryRepository.save(priceHistory);
    }

    /**
     * Fetch active products from product service
     */
    private List<ProductInfo> fetchActiveProducts() {
        try {
            Mono<List> response = webClient.get()
                    .uri(PRODUCT_SERVICE_URL + "/api/products/active")
                    .retrieve()
                    .bodyToMono(List.class);

            List<Map<String, Object>> productMaps = response.block();
            
            return productMaps.stream()
                    .map(this::mapToProductInfo)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching active products", e);
            return List.of();
        }
    }

    /**
     * Map product data to ProductInfo
     */
    private ProductInfo mapToProductInfo(Map<String, Object> productMap) {
        return ProductInfo.builder()
                .id(((Number) productMap.get("id")).longValue())
                .name((String) productMap.get("name"))
                .url((String) productMap.get("url"))
                .targetPrice(productMap.get("targetPrice") != null ? 
                    new BigDecimal(productMap.get("targetPrice").toString()) : null)
                .store((String) productMap.get("store"))
                .userId(((Number) productMap.get("userId")).longValue())
                .selector((String) productMap.get("selector"))
                .build();
    }

    /**
     * Update product's current price
     */
    private void updateProductCurrentPrice(Long productId, BigDecimal currentPrice) {
        try {
            webClient.patch()
                    .uri(PRODUCT_SERVICE_URL + "/api/products/" + productId + "/current-price")
                    .bodyValue(Map.of("currentPrice", currentPrice))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Error updating product current price", e);
        }
    }

    /**
     * Update product status
     */
    private void updateProductStatus(Long productId, String status) {
        try {
            webClient.patch()
                    .uri(PRODUCT_SERVICE_URL + "/api/products/" + productId + "/status")
                    .bodyValue(Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Error updating product status", e);
        }
    }

    /**
     * Update product with error information
     */
    private void updateProductError(Long productId, String error) {
        try {
            webClient.patch()
                    .uri(PRODUCT_SERVICE_URL + "/api/products/" + productId + "/error")
                    .bodyValue(Map.of("error", error, "lastCheckedAt", LocalDateTime.now()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Error updating product error", e);
        }
    }

    /**
     * Publish price change event to message queue
     */
    private void publishPriceChangeEvent(ProductInfo product, BigDecimal previousPrice, BigDecimal currentPrice) {
        try {
            PriceChangeDetectedEvent event = new PriceChangeDetectedEvent(
                    product.getId(),
                    product.getName(),
                    product.getUrl(),
                    previousPrice,
                    currentPrice,
                    product.getTargetPrice(),
                    product.getUserId(),
                    "EMAIL", // Default notification channel
                    null // Phone number will be fetched by notification service
            );

            rabbitTemplate.convertAndSend(PRICE_WATCHER_EXCHANGE, "price.change.detected", event);
            log.debug("Published price change event for product {}", product.getId());
        } catch (Exception e) {
            log.error("Error publishing price change event", e);
        }
    }

    /**
     * Get price history for a product
     */
    public List<PriceHistory> getPriceHistory(Long productId, int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return priceHistoryRepository.findByProductIdAndCheckedAtBetweenOrderByCheckedAtDesc(
                productId, cutoff, LocalDateTime.now());
    }

    /**
     * Get latest price for a product
     */
    public Optional<PriceHistory> getLatestPrice(Long productId) {
        return priceHistoryRepository.findFirstByProductIdOrderByCheckedAtDesc(productId);
    }

    /**
     * Get products with recent price changes
     */
    public List<Long> getProductsWithRecentPriceChanges(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return priceHistoryRepository.findProductsWithPriceChanges(since);
    }
}