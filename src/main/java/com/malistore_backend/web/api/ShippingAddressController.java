package com.malistore_backend.web.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.service.ShippingAddressService;
import com.malistore_backend.web.dto.address.ShippingAddressCreateDto;
import com.malistore_backend.web.dto.address.ShippingAddressResponse;
import com.malistore_backend.web.dto.address.ShippingAddressUpdateDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {
    
    private final ShippingAddressService shippingAddressService;
    private final UserRepository userRepository;
    
    /**
     * Crée une nouvelle adresse de livraison
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> createShippingAddress(
            @Valid @RequestBody ShippingAddressCreateDto shippingAddressCreateDto,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ShippingAddressResponse address = shippingAddressService.createShippingAddress(user, shippingAddressCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(address));
    }
    
    /**
     * Récupère toutes les adresses de livraison de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShippingAddressResponse>>> getUserShippingAddresses(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<ShippingAddressResponse> addresses = shippingAddressService.getUserShippingAddresses(user);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }
    
    /**
     * Récupère une adresse de livraison par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> getUserShippingAddressById(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ShippingAddressResponse address = shippingAddressService.getUserShippingAddressById(user, id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    /**
     * Met à jour une adresse de livraison
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> updateShippingAddress(
            @PathVariable Long id,
            @Valid @RequestBody ShippingAddressUpdateDto shippingAddressUpdateDto,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ShippingAddressResponse address = shippingAddressService.updateShippingAddress(user, id, shippingAddressUpdateDto);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    /**
     * Supprime une adresse de livraison
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteShippingAddress(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        shippingAddressService.deleteShippingAddress(user, id);
        return ResponseEntity.ok(ApiResponse.success("Shipping address deleted successfully"));
    }
    
    /**
     * Récupère l'adresse par défaut de l'utilisateur
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> getDefaultShippingAddress(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ShippingAddressResponse address = shippingAddressService.getDefaultShippingAddress(user);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    /**
     * Définit une adresse comme adresse par défaut
     */
    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<ShippingAddressResponse>> setDefaultShippingAddress(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ShippingAddressResponse address = shippingAddressService.setDefaultShippingAddress(user, id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
}
