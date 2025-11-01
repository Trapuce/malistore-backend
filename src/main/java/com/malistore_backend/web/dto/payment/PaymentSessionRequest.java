package com.malistore_backend.web.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSessionRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    private String successUrl;
    private String cancelUrl;
}



