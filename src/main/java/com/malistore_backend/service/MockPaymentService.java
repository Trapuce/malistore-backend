package com.malistore_backend.service;

import com.malistore_backend.data.entity.*;
import com.malistore_backend.data.repository.OrderRepository;
import com.malistore_backend.data.repository.PaymentRepository;
import com.malistore_backend.web.dto.payment.PaymentResponse;
import com.malistore_backend.web.dto.payment.PaymentSessionRequest;
import com.malistore_backend.web.dto.payment.PaymentSessionResponse;
import com.malistore_backend.web.exception.BadRequestException;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service("mockPaymentService")
@RequiredArgsConstructor
@Slf4j
public class MockPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final InventoryService inventoryService;

    @Value("${stripe.public-key}")
    private String stripePublicKey;

    /**
     * Crée une session de paiement simulée pour une commande
     */
    @Transactional
    public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
        log.info("Creating MOCK payment session for order: {}", request.getOrderId());

        // Récupérer la commande
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        // Vérifier que la commande n'a pas déjà un paiement réussi
        if (paymentRepository.hasSuccessfulPayment(order)) {
            throw new BadRequestException("Order already has a successful payment");
        }

        // Vérifier que la commande est en statut PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status");
        }

        // Générer des IDs simulés
        String mockSessionId = "mock_session_" + UUID.randomUUID().toString().substring(0, 8);
        String mockPaymentIntentId = "mock_pi_" + UUID.randomUUID().toString().substring(0, 8);

        // Créer l'enregistrement de paiement
        Payment payment = Payment.builder()
                .order(order)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .transactionId(mockSessionId)
                .stripeSessionId(mockSessionId)
                .stripePaymentIntentId(mockPaymentIntentId)
                .paymentMethod(PaymentMethod.CARD)
                .currency("EUR")
                .description("Paiement pour la commande " + order.getOrderNumber())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Mock payment record created with ID: {}", payment.getId());

        // Retourner la réponse simulée
        PaymentSessionResponse response = new PaymentSessionResponse();
        response.setSessionId(mockSessionId);
        response.setSessionUrl("https://checkout.stripe.com/mock/" + mockSessionId);
        response.setPublicKey(stripePublicKey);
        response.setOrderId(order.getId());
        response.setMessage("Mock payment session created successfully - Use this for testing");

        return response;
    }

    /**
     * Simule un paiement réussi
     */
    @Transactional
    public PaymentResponse simulateSuccessfulPayment(String sessionId) {
        log.info("Simulating successful payment for session: {}", sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with session ID: " + sessionId));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setWebhookReceivedAt(LocalDateTime.now());

        // Mettre à jour le statut de la commande
        payment.getOrder().setStatus(OrderStatus.PAID);
        orderRepository.save(payment.getOrder());

        // Décrémenter le stock des produits
        try {
            boolean stockDecremented = inventoryService.decrementStockAfterPayment(payment.getOrder().getId());
            if (stockDecremented) {
                log.info("Stock successfully decremented for order {}", payment.getOrder().getId());
            } else {
                log.warn("Failed to decrement stock for order {}", payment.getOrder().getId());
            }
        } catch (Exception e) {
            log.error("Error decrementing stock for order {}: {}", payment.getOrder().getId(), e.getMessage(), e);
        }

        payment = paymentRepository.save(payment);
        log.info("Mock payment completed successfully");

        return paymentMapper.toResponse(payment);
    }

    /**
     * Simule un paiement échoué
     */
    @Transactional
    public PaymentResponse simulateFailedPayment(String sessionId) {
        log.info("Simulating failed payment for session: {}", sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with session ID: " + sessionId));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Mock payment failure for testing");
        payment.setWebhookReceivedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);
        log.info("Mock payment failed");

        return paymentMapper.toResponse(payment);
    }

    /**
     * Récupère les paiements d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(Long userId) {
        log.info("Fetching payments for user: {}", userId);
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return paymentMapper.toResponseList(payments);
    }

    /**
     * Récupère un paiement par ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Récupère les paiements par statut
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        log.info("Fetching payments with status: {}", status);
        List<Payment> payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status);
        return paymentMapper.toResponseList(payments);
    }

    /**
     * Récupère les paiements d'une commande
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getOrderPayments(Long orderId) {
        log.info("Fetching payments for order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        List<Payment> payments = paymentRepository.findByOrderOrderByCreatedAtDesc(order);
        return paymentMapper.toResponseList(payments);
    }
}
