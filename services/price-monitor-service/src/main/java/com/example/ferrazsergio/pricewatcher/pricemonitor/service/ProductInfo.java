package com.example.ferrazsergio.pricewatcher.pricemonitor.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product information used in price monitoring
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfo {
    private Long id;
    private String name;
    private String url;
    private BigDecimal targetPrice;
    private String store;
    private Long userId;
    private String selector;
}