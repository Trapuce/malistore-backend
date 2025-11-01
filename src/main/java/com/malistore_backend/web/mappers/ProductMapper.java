package com.malistore_backend.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.malistore_backend.data.entity.Product;
import com.malistore_backend.web.dto.product.ProductCreateDto;
import com.malistore_backend.web.dto.product.ProductResponse;
import com.malistore_backend.web.dto.product.ProductUpdateDto;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    
    /**
     * Convertit ProductCreateDto vers Product
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toEntity(ProductCreateDto productCreateDto);
    
    /**
     * Convertit Product vers ProductResponse
     */
    ProductResponse toResponse(Product product);
    
    /**
     * Met à jour Product avec ProductUpdateDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEntity(ProductUpdateDto productUpdateDto, @MappingTarget Product product);
}
