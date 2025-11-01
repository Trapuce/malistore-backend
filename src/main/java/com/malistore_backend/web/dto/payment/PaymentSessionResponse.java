package com.malistore_backend.web.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSessionResponse {
    private String sessionId;
    private String sessionUrl;
    private String publicKey;
    private Long orderId;
    private String message;
}



