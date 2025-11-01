package com.malistore_backend.web.mappers;

import com.malistore_backend.data.entity.Payment;
import com.malistore_backend.web.dto.payment.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    
    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(Payment payment);
    
    List<PaymentResponse> toResponseList(List<Payment> payments);
}
