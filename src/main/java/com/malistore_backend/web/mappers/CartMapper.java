package com.malistore_backend.web.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.malistore_backend.data.entity.CartItem;
import com.malistore_backend.web.dto.cart.CartItemResponse;

@Mapper(componentModel = "spring")
public interface CartMapper {
    
    /**
     * Convertit CartItem vers CartItemResponse
     */
    @Mapping(target = "product", source = "product")
    CartItemResponse toResponse(CartItem cartItem);
    
    /**
     * Convertit une liste de CartItem vers une liste de CartItemResponse
     */
    List<CartItemResponse> toResponseList(List<CartItem> cartItems);
}



