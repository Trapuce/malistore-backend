package com.malistore_backend.web.api;

import java.math.BigDecimal;

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
import com.malistore_backend.service.CartService;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.dto.cart.AddToCartDto;
import com.malistore_backend.web.dto.cart.CartItemResponse;
import com.malistore_backend.web.dto.cart.CartResponse;
import com.malistore_backend.web.dto.cart.UpdateCartItemDto;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    private final UserRepository userRepository;
    
    /**
     * Ajoute un produit au panier
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody AddToCartDto addToCartDto,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CartItemResponse cartItem = cartService.addToCart(user, addToCartDto);
        
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }
    
    /**
     * Récupère le contenu du panier
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CartResponse cart = cartService.getCart(user);
        
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
    
    /**
     * Met à jour la quantité d'un article dans le panier
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemDto updateCartItemDto,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CartItemResponse cartItem = cartService.updateCartItem(user, cartItemId, updateCartItemDto);
        
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }
    
    /**
     * Supprime un article du panier
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        cartService.removeFromCart(user, cartItemId);
        
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully"));
    }
    
    /**
     * Vide complètement le panier
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        cartService.clearCart(user);
        
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
    
    /**
     * Récupère le nombre d'articles dans le panier
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Integer count = cartService.getCartItemCount(user);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * Récupère le total du panier
     */
    @GetMapping("/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getCartTotal(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BigDecimal total = cartService.getCartTotal(user);
        
        return ResponseEntity.ok(ApiResponse.success(total));
    }
    
    /**
     * Vérifie si un produit est dans le panier
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> isProductInCart(
            @PathVariable Long productId,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Boolean isInCart = cartService.isProductInCart(user, productId);
        
        return ResponseEntity.ok(ApiResponse.success(isInCart));
    }
    
    /**
     * Récupère la quantité d'un produit dans le panier
     */
    @GetMapping("/quantity/{productId}")
    public ResponseEntity<ApiResponse<Integer>> getProductQuantityInCart(
            @PathVariable Long productId,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Integer quantity = cartService.getProductQuantityInCart(user, productId);
        
        return ResponseEntity.ok(ApiResponse.success(quantity));
    }
}
