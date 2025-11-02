package com.malistore_backend.web.dto.product;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchDto {
    
    private String searchTerm;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // name, price, createdAt
    private String sortDirection; // asc, desc
    private Integer page = 0;
    private Integer size = 10;
}




