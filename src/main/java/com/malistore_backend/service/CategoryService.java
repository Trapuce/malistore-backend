package com.malistore_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.Category;
import com.malistore_backend.data.repository.CategoryRepository;
import com.malistore_backend.web.dto.category.CategoryCreateDto;
import com.malistore_backend.web.dto.category.CategoryResponse;
import com.malistore_backend.web.dto.category.CategoryUpdateDto;
import com.malistore_backend.web.exception.DuplicateResourceException;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.CategoryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    /**
     * Crée une nouvelle catégorie
     */
    public CategoryResponse createCategory(CategoryCreateDto categoryCreateDto) {
        log.info("Creating new category: {}", categoryCreateDto.getName());
        
        // Vérifier si la catégorie existe déjà
        if (categoryRepository.existsByNameIgnoreCase(categoryCreateDto.getName())) {
            throw new DuplicateResourceException("Category with name '" + categoryCreateDto.getName() + "' already exists");
        }
        
        Category category = categoryMapper.toEntity(categoryCreateDto);
        Category savedCategory = categoryRepository.save(category);
        
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return buildCategoryResponse(savedCategory);
    }
    
    /**
     * Récupère toutes les catégories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::buildCategoryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère toutes les catégories actives
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        log.info("Fetching active categories");
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::buildCategoryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère une catégorie par son ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        return buildCategoryResponse(category);
    }
    
    /**
     * Met à jour une catégorie
     */
    public CategoryResponse updateCategory(Long id, CategoryUpdateDto categoryUpdateDto) {
        log.info("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Vérifier si le nouveau nom existe déjà (si fourni)
        if (categoryUpdateDto.getName() != null && 
            !categoryUpdateDto.getName().equalsIgnoreCase(category.getName()) &&
            categoryRepository.existsByNameIgnoreCase(categoryUpdateDto.getName())) {
            throw new DuplicateResourceException("Category with name '" + categoryUpdateDto.getName() + "' already exists");
        }
        
        categoryMapper.updateEntity(categoryUpdateDto, category);
        Category updatedCategory = categoryRepository.save(category);
        
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return buildCategoryResponse(updatedCategory);
    }
    
    /**
     * Supprime une catégorie
     */
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Vérifier s'il y a des produits associés
        Long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with ID " + id + " because it has " + productCount + " associated products");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }
    
    /**
     * Recherche des catégories par nom
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String searchTerm) {
        log.info("Searching categories with term: {}", searchTerm);
        return categoryRepository.findByNameContainingIgnoreCase(searchTerm)
                .stream()
                .map(this::buildCategoryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Active/Désactive une catégorie
     */
    public CategoryResponse toggleCategoryStatus(Long id) {
        log.info("Toggling category status with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        category.setActive(!category.getActive());
        Category updatedCategory = categoryRepository.save(category);
        
        log.info("Category status toggled successfully. New status: {}", updatedCategory.getActive());
        return buildCategoryResponse(updatedCategory);
    }
    
    /**
     * Construit une CategoryResponse avec le nombre de produits
     */
    private CategoryResponse buildCategoryResponse(Category category) {
        CategoryResponse response = categoryMapper.toResponse(category);
        Long productCount = categoryRepository.countProductsByCategoryId(category.getId());
        response.setProductCount(productCount);
        return response;
    }
}
