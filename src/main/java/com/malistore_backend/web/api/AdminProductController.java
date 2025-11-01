package com.malistore_backend.web.api;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.service.InventoryService;
import com.malistore_backend.service.ProductService;
import com.malistore_backend.web.dto.product.ProductResponse;
import com.malistore_backend.web.dto.product.StockUpdateDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    private User getCurrentAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    /**
     * Liste tous les produits avec leurs informations de stock (admin)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProductsWithStock(
            Pageable pageable,
            Authentication authentication) {
        User user = getCurrentAuthenticatedUser(authentication);
        log.info("Admin {} fetching all products with stock", user.getEmail());
        
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Met à jour le stock d'un produit (admin)
     */
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateDto stockUpdateDto,
            Authentication authentication) {
        User user = getCurrentAuthenticatedUser(authentication);
        log.info("Admin {} updating stock for product {} to {}", user.getEmail(), productId, stockUpdateDto.getStock());
        
        inventoryService.updateProductStock(productId, stockUpdateDto.getStock());
        ProductResponse updatedProduct = productService.getProductById(productId);
        
        return ResponseEntity.ok(ApiResponse.success(updatedProduct));
    }

    /**
     * Récupère les produits avec stock faible (admin)
     */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsWithLowStock(
            @RequestParam(defaultValue = "5") Integer threshold,
            Authentication authentication) {
        User user = getCurrentAuthenticatedUser(authentication);
        log.info("Admin {} fetching products with low stock (threshold: {})", user.getEmail(), threshold);
        
        List<ProductResponse> lowStockProducts = inventoryService.getProductsWithLowStock(threshold)
                .stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .imageUrl(product.getImageUrl())
                        .stock(product.getStock())
                        .active(product.getActive())
                        .createdAt(product.getCreatedAt())
                        .updatedAt(product.getUpdatedAt())
                        .build())
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(lowStockProducts));
    }
}