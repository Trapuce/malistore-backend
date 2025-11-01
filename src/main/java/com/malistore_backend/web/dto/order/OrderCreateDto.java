package com.malistore_backend.web.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateDto {
    
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;
    
    @Size(max = 500, message = "Billing address must not exceed 500 characters")
    private String billingAddress;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    // ID de l'adresse de livraison enregistrée (optionnel)
    private Long shippingAddressId;
}
