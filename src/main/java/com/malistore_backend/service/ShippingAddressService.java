package com.malistore_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.ShippingAddress;
import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.ShippingAddressRepository;
import com.malistore_backend.web.dto.address.ShippingAddressCreateDto;
import com.malistore_backend.web.dto.address.ShippingAddressResponse;
import com.malistore_backend.web.dto.address.ShippingAddressUpdateDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.ShippingAddressMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingAddressService {
    
    private final ShippingAddressRepository shippingAddressRepository;
    private final ShippingAddressMapper shippingAddressMapper;
    
    /**
     * Crée une nouvelle adresse de livraison pour un utilisateur
     */
    @Transactional
    public ShippingAddressResponse createShippingAddress(User user, ShippingAddressCreateDto shippingAddressCreateDto) {
        log.info("Creating shipping address for user: {}", user.getEmail());
        
        // Si c'est la première adresse ou si elle est marquée comme défaut, la définir comme défaut
        if (shippingAddressCreateDto.getIsDefault() == null || shippingAddressCreateDto.getIsDefault()) {
            // Désactiver toutes les autres adresses par défaut
            shippingAddressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        shippingAddressRepository.save(existingDefault);
                    });
            
            shippingAddressCreateDto.setIsDefault(true);
        }
        
        ShippingAddress shippingAddress = shippingAddressMapper.toEntity(shippingAddressCreateDto);
        shippingAddress.setUser(user);
        
        shippingAddress = shippingAddressRepository.save(shippingAddress);
        log.info("Shipping address created with ID: {} for user: {}", shippingAddress.getId(), user.getEmail());
        
        return shippingAddressMapper.toResponse(shippingAddress);
    }
    
    /**
     * Récupère toutes les adresses de livraison d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<ShippingAddressResponse> getUserShippingAddresses(User user) {
        log.info("Fetching shipping addresses for user: {}", user.getEmail());
        List<ShippingAddress> addresses = shippingAddressRepository.findByUserOrderByDefaultAndCreatedAtDesc(user);
        return shippingAddressMapper.toResponseList(addresses);
    }
    
    /**
     * Récupère une adresse de livraison par ID pour un utilisateur
     */
    @Transactional(readOnly = true)
    public ShippingAddressResponse getUserShippingAddressById(User user, Long addressId) {
        log.info("Fetching shipping address {} for user: {}", addressId, user.getEmail());
        ShippingAddress address = shippingAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + addressId));
        return shippingAddressMapper.toResponse(address);
    }
    
    /**
     * Met à jour une adresse de livraison
     */
    @Transactional
    public ShippingAddressResponse updateShippingAddress(User user, Long addressId, ShippingAddressUpdateDto shippingAddressUpdateDto) {
        log.info("Updating shipping address {} for user: {}", addressId, user.getEmail());
        
        ShippingAddress address = shippingAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + addressId));
        
        // Si on définit cette adresse comme défaut
        if (Boolean.TRUE.equals(shippingAddressUpdateDto.getIsDefault())) {
            // Désactiver toutes les autres adresses par défaut
            shippingAddressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(existingDefault -> {
                        if (!existingDefault.getId().equals(addressId)) {
                            existingDefault.setIsDefault(false);
                            shippingAddressRepository.save(existingDefault);
                        }
                    });
        }
        
        shippingAddressMapper.updateShippingAddressFromDto(shippingAddressUpdateDto, address);
        address = shippingAddressRepository.save(address);
        
        log.info("Shipping address {} updated successfully for user: {}", addressId, user.getEmail());
        return shippingAddressMapper.toResponse(address);
    }
    
    /**
     * Supprime une adresse de livraison
     */
    @Transactional
    public void deleteShippingAddress(User user, Long addressId) {
        log.info("Deleting shipping address {} for user: {}", addressId, user.getEmail());
        
        ShippingAddress address = shippingAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + addressId));
        
        shippingAddressRepository.delete(address);
        log.info("Shipping address {} deleted successfully for user: {}", addressId, user.getEmail());
    }
    
    /**
     * Récupère l'adresse par défaut d'un utilisateur
     */
    @Transactional(readOnly = true)
    public ShippingAddressResponse getDefaultShippingAddress(User user) {
        log.info("Fetching default shipping address for user: {}", user.getEmail());
        ShippingAddress address = shippingAddressRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new ResourceNotFoundException("No default shipping address found for user"));
        return shippingAddressMapper.toResponse(address);
    }
    
    /**
     * Définit une adresse comme adresse par défaut
     */
    @Transactional
    public ShippingAddressResponse setDefaultShippingAddress(User user, Long addressId) {
        log.info("Setting shipping address {} as default for user: {}", addressId, user.getEmail());
        
        ShippingAddress address = shippingAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + addressId));
        
        // Désactiver toutes les autres adresses par défaut
        shippingAddressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getId().equals(addressId)) {
                        existingDefault.setIsDefault(false);
                        shippingAddressRepository.save(existingDefault);
                    }
                });
        
        // Définir cette adresse comme défaut
        address.setIsDefault(true);
        address = shippingAddressRepository.save(address);
        
        log.info("Shipping address {} set as default for user: {}", addressId, user.getEmail());
        return shippingAddressMapper.toResponse(address);
    }
}
