package com.malistore_backend.web.mappers;

import com.malistore_backend.data.entity.ShippingAddress;
import com.malistore_backend.web.dto.address.ShippingAddressCreateDto;
import com.malistore_backend.web.dto.address.ShippingAddressResponse;
import com.malistore_backend.web.dto.address.ShippingAddressUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShippingAddressMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShippingAddress toEntity(ShippingAddressCreateDto shippingAddressCreateDto);
    
    ShippingAddressResponse toResponse(ShippingAddress shippingAddress);
    
    List<ShippingAddressResponse> toResponseList(List<ShippingAddress> shippingAddresses);
    
    void updateShippingAddressFromDto(ShippingAddressUpdateDto shippingAddressUpdateDto, @MappingTarget ShippingAddress shippingAddress);
}



