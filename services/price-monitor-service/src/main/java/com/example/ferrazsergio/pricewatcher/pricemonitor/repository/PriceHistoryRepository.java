package com.example.ferrazsergio.pricewatcher.pricemonitor.repository;

import com.example.ferrazsergio.pricewatcher.pricemonitor.model.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for price history operations
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    /**
     * Find price history for a specific product
     */
    Page<PriceHistory> findByProductIdOrderByCheckedAtDesc(Long productId, Pageable pageable);

    /**
     * Find the latest price record for a product
     */
    Optional<PriceHistory> findFirstByProductIdOrderByCheckedAtDesc(Long productId);

    /**
     * Find price history within date range
     */
    List<PriceHistory> findByProductIdAndCheckedAtBetweenOrderByCheckedAtDesc(
            Long productId, LocalDateTime start, LocalDateTime end);

    /**
     * Find products that had price changes
     */
    @Query("SELECT DISTINCT ph.productId FROM PriceHistory ph WHERE ph.checkedAt >= :since AND ph.previousPrice IS NOT NULL AND ph.price != ph.previousPrice")
    List<Long> findProductsWithPriceChanges(@Param("since") LocalDateTime since);

    /**
     * Count price checks for a product in a time period
     */
    long countByProductIdAndCheckedAtAfter(Long productId, LocalDateTime since);

    /**
     * Delete old price history records
     */
    void deleteByCheckedAtBefore(LocalDateTime cutoff);

    /**
     * Find average price for a product in a time period
     */
    @Query("SELECT AVG(ph.price) FROM PriceHistory ph WHERE ph.productId = :productId AND ph.checkedAt >= :since AND ph.available = true")
    Double findAveragePriceByProductIdAndSince(@Param("productId") Long productId, @Param("since") LocalDateTime since);
}