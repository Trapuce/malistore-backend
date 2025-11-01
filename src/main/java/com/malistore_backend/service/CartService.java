package com.malistore_backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.CartItem;
import com.malistore_backend.data.entity.Product;
import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.CartItemRepository;
import com.malistore_backend.data.repository.ProductRepository;
import com.malistore_backend.web.dto.cart.AddToCartDto;
import com.malistore_backend.web.dto.cart.CartItemResponse;
import com.malistore_backend.web.dto.cart.CartResponse;
import com.malistore_backend.web.dto.cart.UpdateCartItemDto;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.CartMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    
    /**
     * Ajoute un produit au panier
     */
    public CartItemResponse addToCart(User user, AddToCartDto addToCartDto) {
        log.info("Adding product {} to cart for user {}", addToCartDto.getProductId(), user.getEmail());
        
        // Vérifier que le produit existe et est actif
        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + addToCartDto.getProductId()));
        
        if (!product.getActive()) {
            throw new ResourceNotFoundException("Product is not available");
        }
        
        // Vérifier le stock disponible
        if (product.getStock() < addToCartDto.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
        }
        
        // Vérifier si le produit est déjà dans le panier
        CartItem existingCartItem = cartItemRepository.findByUserAndProduct(user, product).orElse(null);
        
        if (existingCartItem != null) {
            // Mettre à jour la quantité existante
            int newQuantity = existingCartItem.getQuantity() + addToCartDto.getQuantity();
            
            // Vérifier le stock total
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock() + 
                    ", Requested: " + newQuantity);
            }
            
            existingCartItem.updateQuantity(newQuantity);
            existingCartItem.setUnitPrice(product.getPrice());
            CartItem updatedCartItem = cartItemRepository.save(existingCartItem);
            
            log.info("Updated existing cart item for product {} with quantity {}", product.getId(), newQuantity);
            return cartMapper.toResponse(updatedCartItem);
        } else {
            // Créer un nouvel article dans le panier
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(addToCartDto.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            CartItem savedCartItem = cartItemRepository.save(cartItem);
            
            log.info("Added new product {} to cart for user {}", product.getId(), user.getEmail());
            return cartMapper.toResponse(savedCartItem);
        }
    }
    
    /**
     * Récupère le contenu du panier d'un utilisateur
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        log.info("Fetching cart for user {}", user.getEmail());
        
        List<CartItem> cartItems = cartItemRepository.findByUserWithProductDetails(user);
        List<CartItemResponse> cartItemResponses = cartMapper.toResponseList(cartItems);
        
        // Calculer les totaux
        BigDecimal subtotal = cartItemRepository.calculateCartTotal(user);
        Integer totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        CartResponse cartResponse = CartResponse.builder()
                .items(cartItemResponses)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .total(subtotal) // Pour l'instant, pas de taxes ou réductions
                .message("Cart retrieved successfully")
                .build();
        
        log.info("Cart retrieved for user {} with {} items", user.getEmail(), totalItems);
        return cartResponse;
    }
    
    /**
     * Met à jour la quantité d'un article dans le panier
     */
    public CartItemResponse updateCartItem(User user, Long cartItemId, UpdateCartItemDto updateCartItemDto) {
        log.info("Updating cart item {} for user {}", cartItemId, user.getEmail());
        
        CartItem cartItem = cartItemRepository.findByIdAndUser(cartItemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
        
        // Vérifier le stock disponible
        if (cartItem.getProduct().getStock() < updateCartItemDto.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + cartItem.getProduct().getStock());
        }
        
        cartItem.updateQuantity(updateCartItemDto.getQuantity());
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        
        log.info("Updated cart item {} with quantity {}", cartItemId, updateCartItemDto.getQuantity());
        return cartMapper.toResponse(updatedCartItem);
    }
    
    /**
     * Supprime un article du panier
     */
    public void removeFromCart(User user, Long cartItemId) {
        log.info("Removing cart item {} for user {}", cartItemId, user.getEmail());
        
        CartItem cartItem = cartItemRepository.findByIdAndUser(cartItemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
        
        cartItemRepository.delete(cartItem);
        
        log.info("Removed cart item {} from cart for user {}", cartItemId, user.getEmail());
    }
    
    /**
     * Vide complètement le panier d'un utilisateur
     */
    public void clearCart(User user) {
        log.info("Clearing cart for user {}", user.getEmail());
        
        cartItemRepository.deleteByUser(user);
        
        log.info("Cart cleared for user {}", user.getEmail());
    }
    
    /**
     * Récupère le nombre d'articles dans le panier
     */
    @Transactional(readOnly = true)
    public Integer getCartItemCount(User user) {
        Long count = cartItemRepository.countByUser(user);
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * Récupère le total du panier
     */
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(User user) {
        BigDecimal total = cartItemRepository.calculateCartTotal(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Vérifie si un produit est dans le panier
     */
    @Transactional(readOnly = true)
    public boolean isProductInCart(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        return cartItemRepository.existsByUserAndProduct(user, product);
    }
    
    /**
     * Récupère la quantité d'un produit dans le panier
     */
    @Transactional(readOnly = true)
    public Integer getProductQuantityInCart(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        return cartItemRepository.findByUserAndProduct(user, product)
                .map(CartItem::getQuantity)
                .orElse(0);
    }
}
