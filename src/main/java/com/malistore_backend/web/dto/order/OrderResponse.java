package com.malistore_backend.web.dto.order;

import com.malistore_backend.data.entity.OrderStatus;
import com.malistore_backend.web.dto.address.ShippingAddressResponse;
import com.malistore_backend.web.dto.user.UserResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private UserResponse user;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String billingAddress;
    private String notes;
    private ShippingAddressResponse shippingAddressEntity; // Adresse de livraison enregistrée
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
