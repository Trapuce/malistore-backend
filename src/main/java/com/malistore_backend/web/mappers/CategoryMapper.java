package com.malistore_backend.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.malistore_backend.data.entity.Category;
import com.malistore_backend.web.dto.category.CategoryCreateDto;
import com.malistore_backend.web.dto.category.CategoryResponse;
import com.malistore_backend.web.dto.category.CategoryUpdateDto;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    
    /**
     * Convertit CategoryCreateDto vers Category
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryCreateDto categoryCreateDto);
    
    /**
     * Convertit Category vers CategoryResponse
     */
    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toResponse(Category category);
    
    /**
     * Met à jour Category avec CategoryUpdateDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntity(CategoryUpdateDto categoryUpdateDto, @MappingTarget Category category);
}



