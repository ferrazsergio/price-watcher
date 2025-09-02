package com.example.ferrazsergio.pricewatcher.pricemonitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for web scraping product prices from different stores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebScrapingService {

    @Value("${price-monitor.scraping.timeout:10000}")
    private int scrapingTimeout;

    @Value("${price-monitor.scraping.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    // Price regex patterns
    private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9]+(?:[.,][0-9]{3})*(?:[.,][0-9]{2}))");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("R\\$\\s*([0-9.,]+)");

    /**
     * Scrape price from a product URL using custom selector or default selectors
     */
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public BigDecimal scrapePrice(String url, String customSelector, String store) throws IOException {
        log.debug("Scraping price from URL: {} using store: {}", url, store);

        Document document = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(scrapingTimeout)
                .followRedirects(true)
                .get();

        BigDecimal price = null;

        // Try custom selector first
        if (customSelector != null && !customSelector.trim().isEmpty()) {
            price = extractPriceWithSelector(document, customSelector);
            if (price != null) {
                log.debug("Found price {} using custom selector: {}", price, customSelector);
                return price;
            }
        }

        // Fall back to store-specific selectors
        price = extractPriceByStore(document, store, url);
        
        if (price == null) {
            log.warn("Could not extract price from URL: {}", url);
            throw new IllegalStateException("Price not found on page");
        }

        log.debug("Successfully scraped price: {} from {}", price, url);
        return price;
    }

    /**
     * Extract price using specific CSS selector
     */
    private BigDecimal extractPriceWithSelector(Document document, String selector) {
        try {
            Element priceElement = document.selectFirst(selector);
            if (priceElement != null) {
                String priceText = priceElement.text();
                return parsePrice(priceText);
            }
        } catch (Exception e) {
            log.warn("Error extracting price with selector {}: {}", selector, e.getMessage());
        }
        return null;
    }

    /**
     * Extract price using store-specific selectors
     */
    private BigDecimal extractPriceByStore(Document document, String store, String url) {
        switch (store.toUpperCase()) {
            case "AMAZON":
                return extractAmazonPrice(document);
            case "MERCADO_LIVRE":
                return extractMercadoLivrePrice(document);
            case "AMERICANAS":
                return extractAmericanasPrice(document);
            case "MAGAZINE_LUIZA":
                return extractMagazineLuizaPrice(document);
            case "SUBMARINO":
                return extractSubmarinoPrice(document);
            case "CASAS_BAHIA":
                return extractCasasBahiaPrice(document);
            default:
                return extractGenericPrice(document);
        }
    }

    private BigDecimal extractAmazonPrice(Document document) {
        // Amazon price selectors (multiple to handle different layouts)
        String[] selectors = {
                ".a-price-whole",
                ".a-price.a-text-price.a-size-medium.apexPriceToPay .a-offscreen",
                "#priceblock_dealprice",
                "#priceblock_ourprice",
                ".a-price-range",
                ".a-price"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                BigDecimal price = parsePrice(element.text());
                if (price != null) return price;
            }
        }
        return null;
    }

    private BigDecimal extractMercadoLivrePrice(Document document) {
        String[] selectors = {
                ".andes-money-amount__fraction",
                ".price-tag-fraction",
                ".ui-pdp-price__fraction",
                ".andes-money-amount"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                BigDecimal price = parsePrice(element.text());
                if (price != null) return price;
            }
        }
        return null;
    }

    private BigDecimal extractAmericanasPrice(Document document) {
        String[] selectors = {
                "[data-testid='price-value']",
                ".price-template__text",
                ".src__Text-sc"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                BigDecimal price = parsePrice(element.text());
                if (price != null) return price;
            }
        }
        return null;
    }

    private BigDecimal extractMagazineLuizaPrice(Document document) {
        String[] selectors = {
                "[data-testid='price-value']",
                ".price-template__text",
                ".price"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                BigDecimal price = parsePrice(element.text());
                if (price != null) return price;
            }
        }
        return null;
    }

    private BigDecimal extractSubmarinoPrice(Document document) {
        return extractAmericanasPrice(document); // Same selectors as Americanas
    }

    private BigDecimal extractCasasBahiaPrice(Document document) {
        return extractAmericanasPrice(document); // Same selectors as Americanas
    }

    private BigDecimal extractGenericPrice(Document document) {
        // Generic selectors for unknown stores
        String[] selectors = {
                "[class*='price']",
                "[id*='price']",
                "[data-price]",
                ".value",
                ".amount"
        };

        for (String selector : selectors) {
            Element element = document.selectFirst(selector);
            if (element != null) {
                BigDecimal price = parsePrice(element.text());
                if (price != null) return price;
            }
        }
        return null;
    }

    /**
     * Parse price from text using various patterns
     */
    private BigDecimal parsePrice(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        try {
            // Remove currency symbols and extra spaces
            String cleanText = text.replaceAll("[R$\\s]", "");
            
            // Try currency pattern first
            Matcher currencyMatcher = CURRENCY_PATTERN.matcher(text);
            if (currencyMatcher.find()) {
                cleanText = currencyMatcher.group(1);
            }

            // Handle Brazilian number format (dots as thousands separator, comma as decimal)
            if (cleanText.contains(",")) {
                // Check if comma is decimal separator (should be at end)
                int lastComma = cleanText.lastIndexOf(",");
                int lastDot = cleanText.lastIndexOf(".");
                
                if (lastComma > lastDot && cleanText.length() - lastComma == 3) {
                    // Comma is decimal separator
                    cleanText = cleanText.substring(0, lastComma).replace(".", "").replace(",", "") + 
                               "." + cleanText.substring(lastComma + 1);
                } else {
                    // Comma is thousands separator
                    cleanText = cleanText.replace(",", "");
                }
            }

            // Remove any remaining non-digit characters except dot
            cleanText = cleanText.replaceAll("[^0-9.]", "");
            
            if (cleanText.isEmpty()) return null;

            return new BigDecimal(cleanText);
        } catch (NumberFormatException e) {
            log.warn("Could not parse price from text: {}", text);
            return null;
        }
    }

    /**
     * Check if URL is accessible
     */
    public boolean isUrlAccessible(String url) {
        try {
            Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(scrapingTimeout)
                    .followRedirects(true)
                    .execute();
            return true;
        } catch (IOException e) {
            log.warn("URL is not accessible: {}", url);
            return false;
        }
    }
}