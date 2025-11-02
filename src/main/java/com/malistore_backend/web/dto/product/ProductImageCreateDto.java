package com.malistore_backend.web.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageCreateDto {
    
    @NotBlank(message = "Image filename is required")
    @Size(max = 255, message = "Image filename must not exceed 255 characters")
    private String filename;
    
    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    @Size(max = 100, message = "Alt text must not exceed 100 characters")
    private String altText;
    
    private Boolean isPrimary = false;
    
    private Integer sortOrder = 0;
}




