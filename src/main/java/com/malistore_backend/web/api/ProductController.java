package com.malistore_backend.web.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.malistore_backend.service.ProductService;
import com.malistore_backend.web.dto.product.ProductCreateDto;
import com.malistore_backend.web.dto.product.ProductResponse;
import com.malistore_backend.web.dto.product.ProductSearchDto;
import com.malistore_backend.web.dto.product.ProductUpdateDto;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Crée un nouveau produit (Admin seulement)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateDto productCreateDto) {
        ProductResponse product = productService.createProduct(productCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Product created successfully", product, null));
    }
    
    /**
     * Récupère tous les produits avec pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy
            )
        );
        
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Récupère tous les produits actifs (sans pagination)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        List<ProductResponse> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Récupère un produit par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    /**
     * Met à jour un produit (Admin seulement)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateDto productUpdateDto) {
        ProductResponse product = productService.updateProduct(id, productUpdateDto);
        return ResponseEntity.ok(new ApiResponse<>("success", "Product updated successfully", product, null));
    }
    
    /**
     * Supprime un produit (Admin seulement)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
    
    /**
     * Recherche et filtre des produits
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setSearchTerm(searchTerm);
        searchDto.setCategoryId(categoryId);
        searchDto.setMinPrice(minPrice);
        searchDto.setMaxPrice(maxPrice);
        searchDto.setSortBy(sortBy);
        searchDto.setSortDirection(sortDirection);
        searchDto.setPage(page);
        searchDto.setSize(size);
        
        Page<ProductResponse> products = productService.searchProducts(searchDto);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Recherche simple par terme
     */
    @GetMapping("/search/term")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProductsByTerm(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<ProductResponse> products = productService.searchProductsByTerm(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Récupère les produits par catégorie
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Récupère les produits par plage de prix
     */
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Récupère les produits en rupture de stock
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getOutOfStockProducts(
            @RequestParam(defaultValue = "5") Integer stockThreshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getOutOfStockProducts(stockThreshold, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Active/Désactive un produit (Admin seulement)
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProductStatus(@PathVariable Long id) {
        ProductResponse product = productService.toggleProductStatus(id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Product status toggled successfully", product, null));
    }
    
    /**
     * Met à jour le stock d'un produit (Admin seulement)
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStock(
            @PathVariable Long id, 
            @RequestParam Integer stock) {
        ProductResponse product = productService.updateProductStock(id, stock);
        return ResponseEntity.ok(new ApiResponse<>("success", "Product stock updated successfully", product, null));
    }
}




