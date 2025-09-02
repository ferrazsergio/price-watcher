package com.example.ferrazsergio.pricewatcher.analytics.repository;

import com.example.ferrazsergio.pricewatcher.analytics.model.PriceAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for price analytics operations
 */
@Repository
public interface PriceAnalyticsRepository extends JpaRepository<PriceAnalytics, Long> {

    /**
     * Find analytics by product ID and date range
     */
    List<PriceAnalytics> findByProductIdAndDateBetweenOrderByDateAsc(
            Long productId, LocalDate startDate, LocalDate endDate);

    /**
     * Find analytics by product ID and date range with pagination
     */
    Page<PriceAnalytics> findByProductIdAndDateBetweenOrderByDateDesc(
            Long productId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find analytics for a specific product and date
     */
    Optional<PriceAnalytics> findByProductIdAndDate(Long productId, LocalDate date);

    /**
     * Find latest analytics for a product
     */
    Optional<PriceAnalytics> findFirstByProductIdOrderByDateDesc(Long productId);

    /**
     * Find analytics for multiple products on a specific date
     */
    List<PriceAnalytics> findByProductIdInAndDate(List<Long> productIds, LocalDate date);

    /**
     * Calculate average price for a product over a period
     */
    @Query("SELECT AVG(pa.avgPrice) FROM PriceAnalytics pa WHERE pa.productId = :productId AND pa.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateAveragePrice(@Param("productId") Long productId, 
                                   @Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);

    /**
     * Find minimum price in a date range
     */
    @Query("SELECT MIN(pa.minPrice) FROM PriceAnalytics pa WHERE pa.productId = :productId AND pa.date BETWEEN :startDate AND :endDate")
    BigDecimal findMinPriceInRange(@Param("productId") Long productId, 
                                 @Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);

    /**
     * Find maximum price in a date range
     */
    @Query("SELECT MAX(pa.maxPrice) FROM PriceAnalytics pa WHERE pa.productId = :productId AND pa.date BETWEEN :startDate AND :endDate")
    BigDecimal findMaxPriceInRange(@Param("productId") Long productId, 
                                 @Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);

    /**
     * Count volatile days for a product
     */
    @Query("SELECT COUNT(pa) FROM PriceAnalytics pa WHERE pa.productId = :productId AND pa.date BETWEEN :startDate AND :endDate AND (pa.maxPrice - pa.minPrice) / pa.avgPrice > 0.1")
    Long countVolatileDays(@Param("productId") Long productId, 
                          @Param("startDate") LocalDate startDate, 
                          @Param("endDate") LocalDate endDate);

    /**
     * Find products with recent analytics
     */
    @Query("SELECT DISTINCT pa.productId FROM PriceAnalytics pa WHERE pa.date >= :since")
    List<Long> findProductsWithRecentAnalytics(@Param("since") LocalDate since);

    /**
     * Find best price (minimum) for a product
     */
    @Query("SELECT pa FROM PriceAnalytics pa WHERE pa.productId = :productId AND pa.minPrice = (SELECT MIN(pa2.minPrice) FROM PriceAnalytics pa2 WHERE pa2.productId = :productId AND pa2.date BETWEEN :startDate AND :endDate)")
    Optional<PriceAnalytics> findBestPrice(@Param("productId") Long productId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    /**
     * Find worst price (maximum) for a product
     */
    @Query("SELECT pa FROM PriceAnalytics pa WHERE pa.productId = :productId AND pa.maxPrice = (SELECT MAX(pa2.maxPrice) FROM PriceAnalytics pa2 WHERE pa2.productId = :productId AND pa2.date BETWEEN :startDate AND :endDate)")
    Optional<PriceAnalytics> findWorstPrice(@Param("productId") Long productId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    /**
     * Delete old analytics data
     */
    void deleteByDateBefore(LocalDate cutoff);

    /**
     * Check if analytics exist for a product and date
     */
    boolean existsByProductIdAndDate(Long productId, LocalDate date);
}