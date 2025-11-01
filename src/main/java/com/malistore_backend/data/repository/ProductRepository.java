package com.malistore_backend.data.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.malistore_backend.data.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Trouve tous les produits actifs
     */
    List<Product> findByActiveTrue();
    
    /**
     * Trouve tous les produits actifs avec pagination
     */
    Page<Product> findByActiveTrue(Pageable pageable);
    
    /**
     * Trouve les produits par catégorie
     */
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    
    /**
     * Trouve les produits par catégorie avec pagination
     */
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    
    /**
     * Recherche de produits par nom (insensible à la casse)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.active = true")
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Recherche de produits par nom et catégorie
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.category.id = :categoryId AND p.active = true")
    Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndActiveTrue(
        @Param("searchTerm") String searchTerm, 
        @Param("categoryId") Long categoryId, 
        Pageable pageable
    );
    
    /**
     * Recherche de produits par plage de prix
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceBetweenAndActiveTrue(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        Pageable pageable
    );
    
    /**
     * Recherche avancée avec tous les critères
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.active = true")
    Page<Product> findProductsWithFilters(
        @Param("searchTerm") String searchTerm,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
    
    /**
     * Trouve les produits en rupture de stock
     */
    List<Product> findByStockLessThanAndActiveTrue(Integer stockThreshold);
    
    /**
     * Trouve les produits avec stock faible ou égal au seuil
     */
    List<Product> findByStockLessThanEqualAndActiveTrue(Integer stockThreshold);
    
    /**
     * Trouve les produits par stock avec pagination
     */
    Page<Product> findByStockLessThanAndActiveTrue(Integer stockThreshold, Pageable pageable);
    
    /**
     * Compte les produits par catégorie
     */
    Long countByCategoryIdAndActiveTrue(Long categoryId);
    
    /**
     * Trouve les produits les plus récents
     */
    Page<Product> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Trouve les produits par prix croissant
     */
    Page<Product> findByActiveTrueOrderByPriceAsc(Pageable pageable);
    
    /**
     * Trouve les produits par prix décroissant
     */
    Page<Product> findByActiveTrueOrderByPriceDesc(Pageable pageable);
    
    /**
     * Trouve les produits par nom croissant
     */
    Page<Product> findByActiveTrueOrderByNameAsc(Pageable pageable);
}
