package com.example.ferrazsergio.pricewatcher.productservice.controller;

import com.example.ferrazsergio.pricewatcher.common.dto.ApiResponse;
import com.example.ferrazsergio.pricewatcher.productservice.dto.ProductRequest;
import com.example.ferrazsergio.pricewatcher.productservice.dto.ProductResponse;
import com.example.ferrazsergio.pricewatcher.productservice.model.Product;
import com.example.ferrazsergio.pricewatcher.productservice.service.ProductService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for product management
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final MeterRegistry meterRegistry;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Long userId = getUserId(authentication);
            ProductResponse response = productService.createProduct(request, userId);
            
            meterRegistry.counter("products.created", "status", "success").increment();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Product created successfully"));
        } finally {
            sample.stop(Timer.builder("products.create.duration")
                    .description("Time taken to create product")
                    .register(meterRegistry));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        ProductResponse response = productService.getProductById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getUserProducts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Product.ProductCategory category,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        Page<ProductResponse> response;
        
        if (search != null && !search.trim().isEmpty()) {
            response = productService.searchUserProducts(userId, search.trim(), pageable);
        } else if (category != null) {
            response = productService.getUserProductsByCategory(userId, category, pageable);
        } else {
            response = productService.getUserProducts(userId, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByStatus(
            @PathVariable Product.ProductStatus status,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        List<ProductResponse> response = productService.getUserProductsByStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Long userId = getUserId(authentication);
            ProductResponse response = productService.updateProduct(id, request, userId);
            
            meterRegistry.counter("products.updated", "status", "success").increment();
            return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
        } finally {
            sample.stop(Timer.builder("products.update.duration")
                    .description("Time taken to update product")
                    .register(meterRegistry));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        Product.ProductStatus status = Product.ProductStatus.valueOf(statusUpdate.get("status"));
        ProductResponse response = productService.updateProductStatus(id, status, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Product status updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        productService.deleteProduct(id, userId);
        
        meterRegistry.counter("products.deleted", "status", "success").increment();
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProductStats(
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        
        Map<String, Object> stats = Map.of(
                "totalProducts", productService.getUserProductCount(userId),
                "activeProducts", productService.getUserProductCountByStatus(userId, Product.ProductStatus.ACTIVE),
                "pausedProducts", productService.getUserProductCountByStatus(userId, Product.ProductStatus.PAUSED),
                "priceAchievedProducts", productService.getUserPriceAchievedCount(userId)
        );
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // API endpoints for price monitor service

    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        List<ProductResponse> activeProducts = productService.getAllActiveProducts();
        return ResponseEntity.ok(activeProducts);
    }

    @PatchMapping("/{id}/current-price")
    public ResponseEntity<Void> updateCurrentPrice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> priceUpdate) {
        
        productService.updateCurrentPrice(id, priceUpdate);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/error")
    public ResponseEntity<Void> updateProductError(
            @PathVariable Long id,
            @RequestBody Map<String, Object> errorUpdate) {
        
        productService.updateProductError(id, errorUpdate);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            // In a real implementation, you would extract the user ID from the UserDetails
            // For now, we'll use a mock implementation
            return 1L; // TODO: Extract actual user ID from JWT token or UserDetails
        }
        throw new RuntimeException("Authentication required");
    }
}