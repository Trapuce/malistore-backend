package com.malistore_backend.web.dto.order;

import com.malistore_backend.data.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateDto {
    
    @NotNull(message = "Status is required")
    private OrderStatus status;
}




