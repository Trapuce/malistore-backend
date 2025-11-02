package com.malistore_backend.data.repository;

import com.malistore_backend.data.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    /**
     * Find all images for a specific product ordered by sort order
     */
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
    
    /**
     * Find the primary image for a product
     */
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);
    
    /**
     * Count images for a product
     */
    long countByProductId(Long productId);
    
    /**
     * Delete all images for a product
     */
    void deleteByProductId(Long productId);
    
    /**
     * Find images by product ID and primary status
     */
    List<ProductImage> findByProductIdAndIsPrimary(Long productId, Boolean isPrimary);
    
    /**
     * Update primary image - set all images for a product to non-primary
     */
    @Query("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.product.id = :productId")
    void clearPrimaryImages(@Param("productId") Long productId);
}




