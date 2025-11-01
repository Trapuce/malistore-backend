package com.malistore_backend.web.dto.address;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ShippingAddressResponse {
    private Long id;
    private String addressName;
    private String streetAddress;
    private String streetAddressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
