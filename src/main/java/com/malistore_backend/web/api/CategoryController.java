package com.malistore_backend.web.api;

import java.util.List;

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

import com.malistore_backend.service.CategoryService;
import com.malistore_backend.web.dto.category.CategoryCreateDto;
import com.malistore_backend.web.dto.category.CategoryResponse;
import com.malistore_backend.web.dto.category.CategoryUpdateDto;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Crée une nouvelle catégorie
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        CategoryResponse category = categoryService.createCategory(categoryCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Category created successfully", category, null));
    }
    
    /**
     * Récupère toutes les catégories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<CategoryResponse> categories = activeOnly ? 
                categoryService.getActiveCategories() : 
                categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * Récupère une catégorie par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    /**
     * Met à jour une catégorie
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryUpdateDto categoryUpdateDto) {
        CategoryResponse category = categoryService.updateCategory(id, categoryUpdateDto);
        return ResponseEntity.ok(new ApiResponse<>("success", "Category updated successfully", category, null));
    }
    
    /**
     * Supprime une catégorie
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
    
    /**
     * Recherche des catégories par nom
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(@RequestParam String q) {
        List<CategoryResponse> categories = categoryService.searchCategories(q);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * Active/Désactive une catégorie
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleCategoryStatus(@PathVariable Long id) {
        CategoryResponse category = categoryService.toggleCategoryStatus(id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Category status toggled successfully", category, null));
    }
}
