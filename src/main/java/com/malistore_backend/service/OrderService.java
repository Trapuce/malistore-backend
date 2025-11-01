package com.malistore_backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.CartItem;
import com.malistore_backend.data.entity.Order;
import com.malistore_backend.data.entity.OrderItem;
import com.malistore_backend.data.entity.OrderStatus;
import com.malistore_backend.data.entity.Product;
import com.malistore_backend.data.entity.ShippingAddress;
import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.CartItemRepository;
import com.malistore_backend.data.repository.OrderItemRepository;
import com.malistore_backend.data.repository.OrderRepository;
import com.malistore_backend.data.repository.ShippingAddressRepository;
import com.malistore_backend.web.dto.order.OrderCreateDto;
import com.malistore_backend.web.dto.order.OrderResponse;
import com.malistore_backend.web.dto.order.OrderStatusUpdateDto;
import com.malistore_backend.web.exception.BadRequestException;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.OrderMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final OrderMapper orderMapper;
    
    /**
     * Crée une commande à partir du panier de l'utilisateur
     */
    @Transactional
    public OrderResponse createOrderFromCart(User user, OrderCreateDto orderCreateDto) {
        log.info("Creating order from cart for user: {}", user.getEmail());
        
        // Récupérer les articles du panier
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot create order: cart is empty");
        }
        
        // Générer un numéro de commande unique
        String orderNumber = generateOrderNumber();
        
        // Calculer le total d'abord
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            totalAmount = totalAmount.add(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        
        // Récupérer l'adresse de livraison si fournie
        ShippingAddress shippingAddress = null;
        if (orderCreateDto.getShippingAddressId() != null) {
            shippingAddress = shippingAddressRepository.findByIdAndUser(orderCreateDto.getShippingAddressId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));
        }
        
        // Créer la commande
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(orderCreateDto.getShippingAddress())
                .billingAddress(orderCreateDto.getBillingAddress())
                .notes(orderCreateDto.getNotes())
                .shippingAddressEntity(shippingAddress)
                .build();
        
        order = orderRepository.save(order);
        log.info("Order created with ID: {} and number: {}", order.getId(), orderNumber);
        
        // Créer les articles de commande et vérifier le stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Vérifier le stock disponible
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d", 
                        product.getName(), product.getStock(), cartItem.getQuantity())
                );
            }
            
            // Créer l'article de commande
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            orderItem = orderItemRepository.save(orderItem);
            
            // Note: Stock will be decremented only when payment is successful
            // This is handled in PaymentService.updatePaymentStatus() or MockPaymentService.simulateSuccessfulPayment()
            
            log.info("Order item created: {} x {} = {}", 
                product.getName(), cartItem.getQuantity(), orderItem.getTotalPrice());
        }
        
        // Vider le panier
        cartItemRepository.deleteByUser(user);
        log.info("Cart cleared for user: {}", user.getEmail());
        
        log.info("Order created successfully with total: {}", totalAmount);
        return orderMapper.toResponse(order);
    }
    
    /**
     * Récupère les commandes d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(User user) {
        log.info("Fetching orders for user: {}", user.getEmail());
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orderMapper.toResponseList(orders);
    }
    
    /**
     * Récupère une commande par ID pour un utilisateur
     */
    @Transactional(readOnly = true)
    public OrderResponse getUserOrderById(User user, Long orderId) {
        log.info("Fetching order {} for user: {}", orderId, user.getEmail());
        Order order = orderRepository.findByUserAndId(user, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }
    
    /**
     * Récupère toutes les commandes (admin)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponseList(orders);
    }
    
    /**
     * Récupère une commande par ID (admin)
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }
    
    /**
     * Met à jour le statut d'une commande (admin)
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateDto statusUpdateDto) {
        log.info("Updating order {} status to: {}", orderId, statusUpdateDto.getStatus());
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Vérifier la transition de statut valide
        if (!isValidStatusTransition(order.getStatus(), statusUpdateDto.getStatus())) {
            throw new BadRequestException(
                String.format("Invalid status transition from %s to %s", 
                    order.getStatus(), statusUpdateDto.getStatus())
            );
        }
        
        order.setStatus(statusUpdateDto.getStatus());
        order = orderRepository.save(order);
        
        log.info("Order {} status updated to: {}", orderId, statusUpdateDto.getStatus());
        return orderMapper.toResponse(order);
    }
    
    /**
     * Génère un numéro de commande unique
     */
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }
    
    /**
     * Vérifie si la transition de statut est valide
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PAID -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // Statuts finaux
        };
    }
}
