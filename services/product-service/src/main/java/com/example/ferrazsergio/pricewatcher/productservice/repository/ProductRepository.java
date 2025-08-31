package com.example.ferrazsergio.pricewatcher.productservice.repository;

import com.example.ferrazsergio.pricewatcher.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByUserId(Long userId, Pageable pageable);
    
    Page<Product> findByUserIdAndActiveTrue(Long userId, Pageable pageable);
    
    Optional<Product> findByIdAndUserId(Long id, Long userId);
    
    List<Product> findByUserIdAndStatus(Long userId, Product.ProductStatus status);
    
    Page<Product> findByUserIdAndCategory(Long userId, Product.ProductCategory category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.userId = :userId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findByUserIdAndSearchTerm(@Param("userId") Long userId, 
                                          @Param("searchTerm") String searchTerm, 
                                          Pageable pageable);
    
    boolean existsByUrlAndUserId(String url, Long userId);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.active = true")
    List<Product> findActiveProductsForMonitoring();
    
    List<Product> findByStore(Product.SupportedStore store);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, Product.ProductStatus status);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.userId = :userId AND p.currentPrice <= p.targetPrice")
    long countByUserIdAndPriceAchieved(@Param("userId") Long userId);
}