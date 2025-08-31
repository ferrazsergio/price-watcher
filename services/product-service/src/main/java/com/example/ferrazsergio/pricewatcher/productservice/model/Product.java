package com.example.ferrazsergio.pricewatcher.productservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity for monitoring prices
 */
@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = false)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, unique = true, length = 2000)
    private String url;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal targetPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category = ProductCategory.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportedStore store;

    @Column(nullable = false)
    private Long userId;

    @Column
    private String imageUrl;

    @Column
    private String brand;

    @Column
    private String model;

    @Column
    private String selector; // CSS selector for price extraction

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastCheckedAt;

    @Column
    private String lastError;

    @Column(nullable = false)
    private boolean active = true;

    public enum ProductStatus {
        ACTIVE, PAUSED, UNAVAILABLE, PRICE_ACHIEVED
    }

    public enum ProductCategory {
        ELECTRONICS, FASHION, HOME, BOOKS, SPORTS, AUTOMOTIVE, HEALTH, BEAUTY, TOYS, GENERAL
    }

    public enum SupportedStore {
        AMAZON("amazon.com", "Amazon"),
        MERCADO_LIVRE("mercadolivre.com.br", "Mercado Livre"),
        AMERICANAS("americanas.com.br", "Americanas"),
        MAGAZINE_LUIZA("magazineluiza.com.br", "Magazine Luiza"),
        SUBMARINO("submarino.com.br", "Submarino"),
        CASAS_BAHIA("casasbahia.com.br", "Casas Bahia");

        private final String domain;
        private final String displayName;

        SupportedStore(String domain, String displayName) {
            this.domain = domain;
            this.displayName = displayName;
        }

        public String getDomain() {
            return domain;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static SupportedStore fromUrl(String url) {
            if (url == null) return null;
            
            for (SupportedStore store : values()) {
                if (url.toLowerCase().contains(store.domain)) {
                    return store;
                }
            }
            return null;
        }
    }
}