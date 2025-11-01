package com.malistore_backend.web.api;

import com.malistore_backend.service.ProductImageService;
import com.malistore_backend.web.dto.product.ProductImageCreateDto;
import com.malistore_backend.web.dto.product.ProductImageDto;
import com.malistore_backend.web.payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {
    
    private final ProductImageService productImageService;
    
    /**
     * Get all images for a product
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getProductImages(@PathVariable Long productId) {
        log.info("Fetching images for product: {}", productId);
        List<ProductImageDto> images = productImageService.getProductImages(productId);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    /**
     * Get primary image for a product
     */
    @GetMapping("/primary")
    public ResponseEntity<ApiResponse<ProductImageDto>> getPrimaryImage(@PathVariable Long productId) {
        log.info("Fetching primary image for product: {}", productId);
        ProductImageDto image = productImageService.getPrimaryImage(productId);
        return ResponseEntity.ok(ApiResponse.success(image));
    }
    
    /**
     * Add images to a product (from URLs)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> addImages(
            @PathVariable Long productId,
            @Valid @RequestBody List<ProductImageCreateDto> imageDtos) {
        log.info("Adding {} images to product: {}", imageDtos.size(), productId);
        List<ProductImageDto> images = productImageService.addImagesToProduct(productId, imageDtos);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    /**
     * Upload images to a product (from files)
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> uploadImages(
            @PathVariable Long productId,
            @RequestParam("files") MultipartFile[] files) throws IOException {
        log.info("Uploading {} images to product: {}", files.length, productId);
        List<ProductImageDto> images = productImageService.addImagesFromFiles(productId, files);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    /**
     * Upload single image to a product
     */
    @PostMapping("/upload-single")
    public ResponseEntity<ApiResponse<ProductImageDto>> uploadSingleImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary,
            @RequestParam(value = "altText", required = false) String altText) throws IOException {
        log.info("Uploading single image to product: {}", productId);
        ProductImageDto image = productImageService.addSingleImageFromFile(productId, file, isPrimary, altText);
        return ResponseEntity.ok(ApiResponse.success(image));
    }
    
    /**
     * Update an image
     */
    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ProductImageDto>> updateImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @Valid @RequestBody ProductImageCreateDto dto) {
        log.info("Updating image {} for product: {}", imageId, productId);
        ProductImageDto image = productImageService.updateImage(imageId, dto);
        return ResponseEntity.ok(ApiResponse.success(image));
    }
    
    /**
     * Set primary image
     */
    @PutMapping("/{imageId}/primary")
    public ResponseEntity<ApiResponse<ProductImageDto>> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        log.info("Setting image {} as primary for product: {}", imageId, productId);
        ProductImageDto image = productImageService.setPrimaryImage(imageId);
        return ResponseEntity.ok(ApiResponse.success(image));
    }
    
    /**
     * Delete an image
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<String>> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        log.info("Deleting image {} for product: {}", imageId, productId);
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully"));
    }
    
    /**
     * Delete all images for a product
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteAllImages(@PathVariable Long productId) {
        log.info("Deleting all images for product: {}", productId);
        productImageService.deleteAllProductImages(productId);
        return ResponseEntity.ok(ApiResponse.success("All images deleted successfully"));
    }
}
