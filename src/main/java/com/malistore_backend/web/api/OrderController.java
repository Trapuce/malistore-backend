package com.malistore_backend.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.service.OrderService;
import com.malistore_backend.web.dto.order.OrderCreateDto;
import com.malistore_backend.web.dto.order.OrderResponse;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    
    /**
     * Crée une commande à partir du panier de l'utilisateur
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateDto orderCreateDto,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        OrderResponse order = orderService.createOrderFromCart(user, orderCreateDto);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    /**
     * Récupère les commandes de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrderResponse> orders = orderService.getUserOrders(user);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * Récupère une commande par ID pour l'utilisateur connecté
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getUserOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        OrderResponse order = orderService.getUserOrderById(user, id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
