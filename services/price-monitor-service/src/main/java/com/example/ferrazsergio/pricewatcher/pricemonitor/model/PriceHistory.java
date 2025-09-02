package com.example.ferrazsergio.pricewatcher.pricemonitor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for storing price history records
 */
@Entity
@Table(name = "price_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal previousPrice;

    @Column(nullable = false)
    private String source; // Store name

    @Column
    private String currency = "BRL";

    @Column
    private boolean available = true;

    @Column
    private String error;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkedAt;

    @Column
    private String rawData; // For debugging/analysis

    public boolean isPriceChanged() {
        return previousPrice != null && price.compareTo(previousPrice) != 0;
    }

    public boolean isPriceIncreased() {
        return previousPrice != null && price.compareTo(previousPrice) > 0;
    }

    public boolean isPriceDecreased() {
        return previousPrice != null && price.compareTo(previousPrice) < 0;
    }
}