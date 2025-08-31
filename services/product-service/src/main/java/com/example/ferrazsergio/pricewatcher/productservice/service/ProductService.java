package com.example.ferrazsergio.pricewatcher.productservice.service;

import com.example.ferrazsergio.pricewatcher.common.exception.BusinessException;
import com.example.ferrazsergio.pricewatcher.common.exception.ResourceNotFoundException;
import com.example.ferrazsergio.pricewatcher.events.model.ProductCreatedEvent;
import com.example.ferrazsergio.pricewatcher.productservice.dto.ProductRequest;
import com.example.ferrazsergio.pricewatcher.productservice.dto.ProductResponse;
import com.example.ferrazsergio.pricewatcher.productservice.model.Product;
import com.example.ferrazsergio.pricewatcher.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.ferrazsergio.pricewatcher.events.config.RabbitMQConfig.*;

/**
 * Service for product management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ProductValidationService validationService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request, Long userId) {
        log.info("Creating product for user: {}", userId);

        // Validate URL
        validationService.validateProductUrl(request.url());

        // Check if user already has this URL
        if (productRepository.existsByUrlAndUserId(request.url(), userId)) {
            throw new BusinessException("Product with this URL already exists for this user");
        }

        // Detect store from URL
        Product.SupportedStore store = Product.SupportedStore.fromUrl(request.url());
        if (store == null) {
            throw new BusinessException("URL is not from a supported store");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setUrl(request.url());
        product.setTargetPrice(request.targetPrice());
        product.setCategory(request.category() != null ? request.category() : Product.ProductCategory.GENERAL);
        product.setStore(store);
        product.setUserId(userId);
        product.setImageUrl(request.imageUrl());
        product.setBrand(request.brand());
        product.setModel(request.model());
        product.setSelector(request.selector());
        product.setStatus(Product.ProductStatus.ACTIVE);

        product = productRepository.save(product);

        // Publish product created event
        ProductCreatedEvent event = new ProductCreatedEvent(
                product.getId(),
                product.getName(),
                product.getUrl(),
                product.getTargetPrice(),
                product.getUserId(),
                product.getStore().name()
        );
        rabbitTemplate.convertAndSend(PRICE_WATCHER_EXCHANGE, "product.created", event);

        log.info("Product created successfully with ID: {}", product.getId());
        return mapToResponse(product);
    }

    @Cacheable(value = "products", key = "#id + '_' + #userId")
    public ProductResponse getProductById(Long id, Long userId) {
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return mapToResponse(product);
    }

    @Cacheable(value = "userProducts", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponse> getUserProducts(Long userId, Pageable pageable) {
        return productRepository.findByUserIdAndActiveTrue(userId, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> searchUserProducts(Long userId, String searchTerm, Pageable pageable) {
        return productRepository.findByUserIdAndSearchTerm(userId, searchTerm, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> getUserProductsByCategory(Long userId, Product.ProductCategory category, Pageable pageable) {
        return productRepository.findByUserIdAndCategory(userId, category, pageable)
                .map(this::mapToResponse);
    }

    public List<ProductResponse> getUserProductsByStatus(Long userId, Product.ProductStatus status) {
        return productRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {"products", "userProducts"}, allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request, Long userId) {
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // If URL is changing, validate it and check for duplicates
        if (!product.getUrl().equals(request.url())) {
            validationService.validateProductUrl(request.url());
            
            if (productRepository.existsByUrlAndUserId(request.url(), userId)) {
                throw new BusinessException("Product with this URL already exists for this user");
            }
            
            Product.SupportedStore newStore = Product.SupportedStore.fromUrl(request.url());
            if (newStore == null) {
                throw new BusinessException("URL is not from a supported store");
            }
            product.setStore(newStore);
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setUrl(request.url());
        product.setTargetPrice(request.targetPrice());
        product.setCategory(request.category() != null ? request.category() : product.getCategory());
        product.setImageUrl(request.imageUrl());
        product.setBrand(request.brand());
        product.setModel(request.model());
        product.setSelector(request.selector());

        product = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", product.getId());
        return mapToResponse(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "userProducts"}, allEntries = true)
    public void deleteProduct(Long id, Long userId) {
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft deleted with ID: {}", id);
    }

    @Transactional
    @CacheEvict(value = {"products", "userProducts"}, allEntries = true)
    public ProductResponse updateProductStatus(Long id, Product.ProductStatus status, Long userId) {
        Product product = productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setStatus(status);
        product = productRepository.save(product);
        
        log.info("Product status updated to {} for ID: {}", status, id);
        return mapToResponse(product);
    }

    public long getUserProductCount(Long userId) {
        return productRepository.countByUserId(userId);
    }

    public long getUserProductCountByStatus(Long userId, Product.ProductStatus status) {
        return productRepository.countByUserIdAndStatus(userId, status);
    }

    public long getUserPriceAchievedCount(Long userId) {
        return productRepository.countByUserIdAndPriceAchieved(userId);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getUrl(),
                product.getTargetPrice(),
                product.getCurrentPrice(),
                product.getStatus(),
                product.getCategory(),
                product.getStore(),
                product.getUserId(),
                product.getImageUrl(),
                product.getBrand(),
                product.getModel(),
                product.getSelector(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getLastCheckedAt(),
                product.getLastError(),
                product.isActive()
        );
    }
}