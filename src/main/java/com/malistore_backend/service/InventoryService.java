package com.malistore_backend.service;

import com.malistore_backend.data.entity.Order;
import com.malistore_backend.data.entity.OrderItem;
import com.malistore_backend.data.entity.OrderStatus;
import com.malistore_backend.data.entity.Product;
import com.malistore_backend.data.repository.OrderRepository;
import com.malistore_backend.data.repository.ProductRepository;
import com.malistore_backend.web.exception.BadRequestException;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Decrement stock for all products in an order after successful payment
     * @param orderId The order ID
     * @return true if stock was successfully decremented, false if insufficient stock
     */
    @Transactional
    public boolean decrementStockAfterPayment(Long orderId) {
        log.info("Starting stock decrement for order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Order {} is not in PAID status, cannot decrement stock. Current status: {}", 
                    orderId, order.getStatus());
            return false;
        }

        // First, verify that all products have sufficient stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                log.error("Insufficient stock for product {} (ID: {}). Required: {}, Available: {}", 
                        product.getName(), product.getId(), item.getQuantity(), product.getStock());
                throw new BadRequestException("Insufficient stock for product: " + product.getName() + 
                        ". Required: " + item.getQuantity() + ", Available: " + product.getStock());
            }
        }

        // If we reach here, all products have sufficient stock, proceed with decrement
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() - item.getQuantity();
            product.setStock(newStock);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            log.info("Stock decremented for product {} (ID: {}). New stock: {}", 
                    product.getName(), product.getId(), newStock);
        }

        log.info("Successfully decremented stock for all products in order: {}", orderId);
        return true;
    }

    /**
     * Check if there's sufficient stock for all products in an order
     * @param orderId The order ID
     * @return true if all products have sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                log.warn("Insufficient stock for product {} (ID: {}). Required: {}, Available: {}", 
                        product.getName(), product.getId(), item.getQuantity(), product.getStock());
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a product has sufficient stock for a given quantity
     * @param productId The product ID
     * @param quantity The required quantity
     * @return true if product has sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean checkProductStockAvailability(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        return product.getStock() >= quantity;
    }

    /**
     * Update product stock manually (admin function)
     * @param productId The product ID
     * @param newStock The new stock quantity
     * @return The updated product
     */
    @Transactional
    public Product updateProductStock(Long productId, Integer newStock) {
        if (newStock < 0) {
            throw new BadRequestException("Stock cannot be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        int oldStock = product.getStock();
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);

        log.info("Stock updated for product {} (ID: {}). Old stock: {}, New stock: {}", 
                product.getName(), productId, oldStock, newStock);

        return updatedProduct;
    }

    /**
     * Get products with low stock (below threshold)
     * @param threshold The stock threshold
     * @return List of products with low stock
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsWithLowStock(Integer threshold) {
        return productRepository.findByStockLessThanEqualAndActiveTrue(threshold);
    }

    /**
     * Get all products with their stock information
     * @return List of all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithStock() {
        return productRepository.findAll();
    }
}
