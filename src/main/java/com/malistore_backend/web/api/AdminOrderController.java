package com.malistore_backend.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.service.OrderService;
import com.malistore_backend.web.dto.order.OrderResponse;
import com.malistore_backend.web.dto.order.OrderStatusUpdateDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    
    /**
     * Récupère toutes les commandes (admin)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(Authentication authentication) {
        // Vérifier que l'utilisateur est admin
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied. Admin role required."));
        }
        
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * Récupère une commande par ID (admin)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Vérifier que l'utilisateur est admin
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied. Admin role required."));
        }
        
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    /**
     * Met à jour le statut d'une commande (admin)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDto statusUpdateDto,
            Authentication authentication) {
        
        // Vérifier que l'utilisateur est admin
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied. Admin role required."));
        }
        
        OrderResponse order = orderService.updateOrderStatus(id, statusUpdateDto);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}



