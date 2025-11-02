package com.malistore_backend.web.api;

import com.malistore_backend.service.ProductService;
import com.malistore_backend.service.ProductImageService;
import com.malistore_backend.web.dto.product.ProductResponse;
import com.malistore_backend.web.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products/upload")
@RequiredArgsConstructor
@Slf4j
public class ProductUploadController {
    
    private final ProductService productService;
    private final ProductImageService productImageService;
    
    /**
     * Upload product with multiple images
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> uploadProductWithImages(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "altTexts", required = false) String[] altTexts) throws IOException {
        
        log.info("Uploading product with {} images: {}", 
                files != null ? files.length : 0, name);
        
        // Create product first
        ProductResponse product = productService.createProduct(
            com.malistore_backend.web.dto.product.ProductCreateDto.builder()
                .name(name)
                .description(description)
                .price(java.math.BigDecimal.valueOf(price))
                .stock(stock)
                .categoryId(categoryId)
                .build()
        );
        
        // Add images if provided
        if (files != null && files.length > 0) {
            List<com.malistore_backend.web.dto.product.ProductImageDto> images = 
                productImageService.addImagesFromFiles(product.getId(), files);
            
            // Update alt texts if provided
            if (altTexts != null && altTexts.length > 0) {
                for (int i = 0; i < Math.min(images.size(), altTexts.length); i++) {
                    if (altTexts[i] != null && !altTexts[i].trim().isEmpty()) {
                        // Update image with custom alt text
                        com.malistore_backend.web.dto.product.ProductImageCreateDto updateDto = 
                            com.malistore_backend.web.dto.product.ProductImageCreateDto.builder()
                                .filename(images.get(i).getFilename())
                                .imageUrl(images.get(i).getImageUrl())
                                .altText(altTexts[i])
                                .isPrimary(images.get(i).getIsPrimary())
                                .sortOrder(images.get(i).getSortOrder())
                                .build();
                        
                        productImageService.updateImage(images.get(i).getId(), updateDto);
                    }
                }
            }
        }
        
        // Return updated product with images
        ProductResponse updatedProduct = productService.getProductById(product.getId());
        return ResponseEntity.ok(ApiResponse.success(updatedProduct));
    }
    
    /**
     * Upload images to existing product
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<List<com.malistore_backend.web.dto.product.ProductImageDto>>> uploadImagesToProduct(
            @PathVariable Long productId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "altTexts", required = false) String[] altTexts) throws IOException {
        
        log.info("Uploading {} images to product: {}", files.length, productId);
        
        List<com.malistore_backend.web.dto.product.ProductImageDto> images = 
            productImageService.addImagesFromFiles(productId, files);
        
        // Update alt texts if provided
        if (altTexts != null && altTexts.length > 0) {
            for (int i = 0; i < Math.min(images.size(), altTexts.length); i++) {
                if (altTexts[i] != null && !altTexts[i].trim().isEmpty()) {
                    com.malistore_backend.web.dto.product.ProductImageCreateDto updateDto = 
                        com.malistore_backend.web.dto.product.ProductImageCreateDto.builder()
                            .filename(images.get(i).getFilename())
                            .imageUrl(images.get(i).getImageUrl())
                            .altText(altTexts[i])
                            .isPrimary(images.get(i).getIsPrimary())
                            .sortOrder(images.get(i).getSortOrder())
                            .build();
                    
                    productImageService.updateImage(images.get(i).getId(), updateDto);
                }
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(images));
    }
}




