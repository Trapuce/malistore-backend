package com.malistore_backend.web.mappers;

import com.malistore_backend.data.entity.OrderItem;
import com.malistore_backend.web.dto.order.OrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface OrderItemMapper {
    
    @Mapping(target = "totalPrice", expression = "java(orderItem.getUnitPrice().multiply(new java.math.BigDecimal(orderItem.getQuantity())))")
    OrderItemResponse toResponse(OrderItem orderItem);
    
    List<OrderItemResponse> toResponseList(List<OrderItem> orderItems);
}




