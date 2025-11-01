package com.malistore_backend.web.dto.address;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAddressUpdateDto {
    
    @Size(min = 2, max = 100, message = "Address name must be between 2 and 100 characters")
    private String addressName;
    
    @Size(max = 200, message = "Street address must not exceed 200 characters")
    private String streetAddress;
    
    @Size(max = 100, message = "Street address line 2 must not exceed 100 characters")
    private String streetAddressLine2;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "State/Province must not exceed 100 characters")
    private String stateProvince;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    private Boolean isDefault;
}
