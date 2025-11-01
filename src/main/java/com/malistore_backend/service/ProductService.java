package com.malistore_backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.Category;
import com.malistore_backend.data.entity.Product;
import com.malistore_backend.data.repository.CategoryRepository;
import com.malistore_backend.data.repository.ProductRepository;
import com.malistore_backend.web.dto.product.ProductCreateDto;
import com.malistore_backend.web.dto.product.ProductResponse;
import com.malistore_backend.web.dto.product.ProductSearchDto;
import com.malistore_backend.web.dto.product.ProductUpdateDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.ProductMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductImageService productImageService;
    
    /**
     * Crée un nouveau produit
     */
    public ProductResponse createProduct(ProductCreateDto productCreateDto) {
        log.info("Creating new product: {}", productCreateDto.getName());
        
        // Vérifier que la catégorie existe
        Category category = categoryRepository.findById(productCreateDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productCreateDto.getCategoryId()));
        
        Product product = productMapper.toEntity(productCreateDto);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        
        // Handle multiple images if provided
        if (productCreateDto.getImages() != null && !productCreateDto.getImages().isEmpty()) {
            log.info("Adding {} images to product: {}", productCreateDto.getImages().size(), savedProduct.getId());
            productImageService.addImagesToProduct(savedProduct.getId(), productCreateDto.getImages());
        }
        
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return getProductById(savedProduct.getId());
    }
    
    /**
     * Récupère tous les produits avec pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toResponse);
    }
    
    /**
     * Récupère tous les produits actifs
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActiveProducts() {
        log.info("Fetching all active products");
        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère un produit par son ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        ProductResponse response = productMapper.toResponse(product);
        
        // Add images to response
        response.setImages(productImageService.getProductImages(id));
        
        return response;
    }
    
    /**
     * Met à jour un produit
     */
    public ProductResponse updateProduct(Long id, ProductUpdateDto productUpdateDto) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Vérifier la catégorie si elle est fournie
        if (productUpdateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productUpdateDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productUpdateDto.getCategoryId()));
            product.setCategory(category);
        }
        
        productMapper.updateEntity(productUpdateDto, product);
        Product updatedProduct = productRepository.save(product);
        
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }
    
    /**
     * Supprime un produit
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        productRepository.delete(product);
        log.info("Product deleted successfully with ID: {}", id);
    }
    
    /**
     * Recherche et filtre des produits
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchDto searchDto) {
        log.info("Searching products with filters: {}", searchDto);
        
        // Créer le Pageable avec tri
        Pageable pageable = createPageable(searchDto);
        
        // Utiliser la méthode de recherche avancée
        Page<Product> products = productRepository.findProductsWithFilters(
                searchDto.getSearchTerm(),
                searchDto.getCategoryId(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                pageable
        );
        
        return products.map(productMapper::toResponse);
    }
    
    /**
     * Recherche simple par terme
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductsByTerm(String searchTerm, Pageable pageable) {
        log.info("Searching products with term: {}", searchTerm);
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm, pageable)
                .map(productMapper::toResponse);
    }
    
    /**
     * Récupère les produits par catégorie
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products for category ID: {}", categoryId);
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(productMapper::toResponse);
    }
    
    /**
     * Récupère les produits par plage de prix
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.info("Fetching products with price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice, pageable)
                .map(productMapper::toResponse);
    }
    
    /**
     * Récupère les produits en rupture de stock
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getOutOfStockProducts(Integer stockThreshold, Pageable pageable) {
        log.info("Fetching out of stock products with threshold: {}", stockThreshold);
        return productRepository.findByStockLessThanAndActiveTrue(stockThreshold, pageable)
                .map(productMapper::toResponse);
    }
    
    /**
     * Active/Désactive un produit
     */
    public ProductResponse toggleProductStatus(Long id) {
        log.info("Toggling product status with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        product.setActive(!product.getActive());
        Product updatedProduct = productRepository.save(product);
        
        log.info("Product status toggled successfully. New status: {}", updatedProduct.getActive());
        return productMapper.toResponse(updatedProduct);
    }
    
    /**
     * Met à jour le stock d'un produit
     */
    public ProductResponse updateProductStock(Long id, Integer newStock) {
        log.info("Updating product stock for ID: {} to {}", id, newStock);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        
        log.info("Product stock updated successfully for ID: {}", id);
        return productMapper.toResponse(updatedProduct);
    }
    
    /**
     * Crée un Pageable avec tri basé sur les critères de recherche
     */
    private Pageable createPageable(ProductSearchDto searchDto) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name"); // Tri par défaut
        
        if (searchDto.getSortBy() != null && searchDto.getSortDirection() != null) {
            Sort.Direction direction = searchDto.getSortDirection().equalsIgnoreCase("desc") 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, searchDto.getSortBy());
        }
        
        return PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);
    }
}
