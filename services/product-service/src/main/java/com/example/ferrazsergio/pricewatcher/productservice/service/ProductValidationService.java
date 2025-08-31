package com.example.ferrazsergio.pricewatcher.productservice.service;

import com.example.ferrazsergio.pricewatcher.common.exception.BusinessException;
import com.example.ferrazsergio.pricewatcher.productservice.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Service for validating product data
 */
@Service
@Slf4j
public class ProductValidationService {

    private static final List<String> SUPPORTED_DOMAINS = Arrays.asList(
            "amazon.com", "amazon.com.br",
            "mercadolivre.com.br",
            "americanas.com.br",
            "magazineluiza.com.br",
            "submarino.com.br",
            "casasbahia.com.br"
    );

    public void validateProductUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new BusinessException("Product URL is required");
        }

        try {
            URL urlObj = new URL(url);
            String host = urlObj.getHost().toLowerCase();
            
            // Remove www. prefix if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            final String finalHost = host; // Make it effectively final for lambda
            boolean isSupported = SUPPORTED_DOMAINS.stream()
                    .anyMatch(domain -> finalHost.equals(domain) || finalHost.endsWith("." + domain));

            if (!isSupported) {
                throw new BusinessException("URL is not from a supported store. Supported stores: " + 
                        String.join(", ", SUPPORTED_DOMAINS));
            }

            // Additional validation - ensure it's a product URL
            validateProductUrlStructure(url, finalHost);

        } catch (java.net.MalformedURLException e) {
            throw new BusinessException("Invalid URL format: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("Invalid URL format: " + e.getMessage());
        }
    }

    private void validateProductUrlStructure(String url, String host) {
        // Amazon validation
        if (host.contains("amazon")) {
            if (!url.contains("/dp/") && !url.contains("/gp/product/")) {
                throw new BusinessException("Amazon URL must be a valid product URL (containing /dp/ or /gp/product/)");
            }
        }
        
        // Mercado Livre validation
        else if (host.contains("mercadolivre")) {
            if (!url.contains("MLB-") && !url.contains("/p/")) {
                throw new BusinessException("Mercado Livre URL must be a valid product URL");
            }
        }
        
        // Other stores can have basic validation
        // This can be extended with more specific rules per store
    }

    public Product.SupportedStore detectStore(String url) {
        return Product.SupportedStore.fromUrl(url);
    }
}