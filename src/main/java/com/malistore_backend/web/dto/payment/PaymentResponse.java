package com.malistore_backend.web.dto.payment;

import com.malistore_backend.data.entity.PaymentMethod;
import com.malistore_backend.data.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String transactionId;
    private String stripePaymentIntentId;
    private String stripeSessionId;
    private PaymentMethod paymentMethod;
    private String currency;
    private String description;
    private String failureReason;
    private LocalDateTime webhookReceivedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




