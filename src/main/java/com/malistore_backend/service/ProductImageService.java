package com.malistore_backend.service;

import com.malistore_backend.data.entity.Product;
import com.malistore_backend.data.entity.ProductImage;
import com.malistore_backend.data.repository.ProductImageRepository;
import com.malistore_backend.web.dto.product.ProductImageCreateDto;
import com.malistore_backend.web.dto.product.ProductImageDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductImageService {
    
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;
    
    /**
     * Add images to a product
     */
    public List<ProductImageDto> addImagesToProduct(Long productId, List<ProductImageCreateDto> imageDtos) {
        log.info("Adding {} images to product {}", imageDtos.size(), productId);
        
        List<ProductImage> images = imageDtos.stream()
                .map(dto -> ProductImage.builder()
                        .filename(dto.getFilename())
                        .imageUrl(dto.getImageUrl())
                        .altText(dto.getAltText())
                        .isPrimary(dto.getIsPrimary())
                        .sortOrder(dto.getSortOrder())
                        .product(Product.builder().id(productId).build())
                        .build())
                .collect(Collectors.toList());
        
        List<ProductImage> savedImages = productImageRepository.saveAll(images);
        
        // Ensure only one primary image
        if (savedImages.stream().anyMatch(ProductImage::getIsPrimary)) {
            ensureSinglePrimaryImage(productId);
        }
        
        return savedImages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Add images from uploaded files
     */
    public List<ProductImageDto> addImagesFromFiles(Long productId, MultipartFile[] files) throws IOException {
        log.info("Adding {} uploaded images to product {}", files.length, productId);
        
        // Validate all files
        for (MultipartFile file : files) {
            if (!imageStorageService.isValidImageFile(file)) {
                throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
            }
        }
        
        // Store images and get URLs
        String[] imageUrls = imageStorageService.storeImages(files);
        
        // Create image DTOs
        List<ProductImageCreateDto> imageDtos = new java.util.ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            imageDtos.add(ProductImageCreateDto.builder()
                    .filename(files[i].getOriginalFilename())
                    .imageUrl(imageUrls[i])
                    .altText("Product image " + (i + 1))
                    .isPrimary(i == 0) // First image is primary
                    .sortOrder(i)
                    .build());
        }
        
        return addImagesToProduct(productId, imageDtos);
    }
    
    /**
     * Add single image from uploaded file
     */
    public ProductImageDto addSingleImageFromFile(Long productId, MultipartFile file, Boolean isPrimary, String altText) throws IOException {
        log.info("Adding single uploaded image to product {}", productId);
        
        // Validate file
        if (!imageStorageService.isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
        }
        
        // Store image and get URL
        String imageUrl = imageStorageService.storeImage(file);
        
        // Create image DTO
        ProductImageCreateDto imageDto = ProductImageCreateDto.builder()
                .filename(file.getOriginalFilename())
                .imageUrl(imageUrl)
                .altText(altText != null ? altText : "Product image")
                .isPrimary(isPrimary)
                .sortOrder(0) // Will be adjusted based on existing images
                .build();
        
        // Get current image count for sort order
        long currentImageCount = productImageRepository.countByProductId(productId);
        imageDto.setSortOrder((int) currentImageCount);
        
        List<ProductImageDto> images = addImagesToProduct(productId, List.of(imageDto));
        return images.get(0);
    }
    
    /**
     * Get all images for a product
     */
    @Transactional(readOnly = true)
    public List<ProductImageDto> getProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
        return images.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get primary image for a product
     */
    @Transactional(readOnly = true)
    public ProductImageDto getPrimaryImage(Long productId) {
        return productImageRepository.findByProductIdAndIsPrimaryTrue(productId)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    /**
     * Update image details
     */
    public ProductImageDto updateImage(Long imageId, ProductImageCreateDto dto) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        image.setFilename(dto.getFilename());
        image.setImageUrl(dto.getImageUrl());
        image.setAltText(dto.getAltText());
        image.setIsPrimary(dto.getIsPrimary());
        image.setSortOrder(dto.getSortOrder());
        
        ProductImage savedImage = productImageRepository.save(image);
        
        // Ensure only one primary image
        if (savedImage.getIsPrimary()) {
            ensureSinglePrimaryImage(savedImage.getProduct().getId());
        }
        
        return convertToDto(savedImage);
    }
    
    /**
     * Delete an image
     */
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        // Delete physical file
        imageStorageService.deleteImage(image.getImageUrl());
        
        // Delete from database
        productImageRepository.delete(image);
        
        log.info("Image deleted: {}", imageId);
    }
    
    /**
     * Delete all images for a product
     */
    public void deleteAllProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
        
        // Delete physical files
        images.forEach(image -> imageStorageService.deleteImage(image.getImageUrl()));
        
        // Delete from database
        productImageRepository.deleteByProductId(productId);
        
        log.info("All images deleted for product: {}", productId);
    }
    
    /**
     * Set primary image
     */
    public ProductImageDto setPrimaryImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        // Clear all primary images for this product
        productImageRepository.clearPrimaryImages(image.getProduct().getId());
        
        // Set this image as primary
        image.setIsPrimary(true);
        ProductImage savedImage = productImageRepository.save(image);
        
        return convertToDto(savedImage);
    }
    
    /**
     * Ensure only one primary image per product
     */
    private void ensureSinglePrimaryImage(Long productId) {
        List<ProductImage> primaryImages = productImageRepository.findByProductIdAndIsPrimary(productId, true);
        if (primaryImages.size() > 1) {
            // Keep only the first one as primary
            for (int i = 1; i < primaryImages.size(); i++) {
                primaryImages.get(i).setIsPrimary(false);
            }
            productImageRepository.saveAll(primaryImages);
        }
    }
    
    /**
     * Convert entity to DTO
     */
    private ProductImageDto convertToDto(ProductImage image) {
        return ProductImageDto.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .isPrimary(image.getIsPrimary())
                .sortOrder(image.getSortOrder())
                .build();
    }
}
