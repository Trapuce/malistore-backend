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
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final InventoryService inventoryService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.public-key}")
    private String stripePublicKey;

    @Value("${stripe.success-url}")
    private String defaultSuccessUrl;

    @Value("${stripe.cancel-url}")
    private String defaultCancelUrl;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty() && stripeSecretKey.length() > 7) {
            Stripe.apiKey = stripeSecretKey;
            log.info("Stripe initialized with secret key: {}...", stripeSecretKey.substring(0, 7));
        } else {
            log.warn("Stripe secret key not configured. Payment features will be disabled.");
        }
    }

    /**
     * Crée une session de paiement Stripe pour une commande
     */
    @Transactional
    public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
        log.info("Creating payment session for order: {}", request.getOrderId());

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

        try {
            // Créer les line items pour Stripe
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(orderItem.getProduct().getName())
                                        .setDescription(orderItem.getProduct().getDescription())
                                        .build())
                                .setUnitAmount(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(100)).longValue()) // Convertir en centimes
                                .build())
                        .setQuantity((long) orderItem.getQuantity())
                        .build();
                lineItems.add(lineItem);
            }

            // Créer la session Stripe
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.getSuccessUrl() != null ? request.getSuccessUrl() : defaultSuccessUrl)
                    .setCancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : defaultCancelUrl)
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                            .setDescription("Paiement pour la commande " + order.getOrderNumber())
                            .build())
                    .addAllLineItem(lineItems)
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("order_number", order.getOrderNumber())
                    .build();

            Session session = Session.create(params);
            log.info("Stripe session created: {}", session.getId());

            // Créer l'enregistrement de paiement
            Payment payment = Payment.builder()
                    .order(order)
                    .status(PaymentStatus.PENDING)
                    .amount(order.getTotalAmount())
                    .transactionId(session.getId())
                    .stripeSessionId(session.getId())
                    .paymentMethod(PaymentMethod.CARD)
                    .currency("EUR")
                    .description("Paiement pour la commande " + order.getOrderNumber())
                    .build();

            payment = paymentRepository.save(payment);
            log.info("Payment record created with ID: {}", payment.getId());

            // Retourner la réponse
            PaymentSessionResponse response = new PaymentSessionResponse();
            response.setSessionId(session.getId());
            response.setSessionUrl(session.getUrl());
            response.setPublicKey(stripePublicKey);
            response.setOrderId(order.getId());
            response.setMessage("Payment session created successfully");

            return response;

        } catch (StripeException e) {
            log.error("Error creating Stripe session: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to create payment session: " + e.getMessage());
        }
    }

    /**
     * Traite un webhook Stripe
     */
    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        log.info("Processing Stripe webhook");

        try {
            // Ici, vous devriez vérifier la signature du webhook
            // Pour simplifier, on va traiter directement le payload
            
            // Dans un vrai projet, vous devriez utiliser:
            // Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            
            // Pour la démo, on va simuler le traitement
            log.info("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to process webhook: " + e.getMessage());
        }
    }

    /**
     * Met à jour le statut d'un paiement
     */
    @Transactional
    public PaymentResponse updatePaymentStatus(String sessionId, PaymentStatus status, String paymentIntentId) {
        log.info("Updating payment status for session: {} to {}", sessionId, status);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with session ID: " + sessionId));

        payment.setStatus(status);
        payment.setStripePaymentIntentId(paymentIntentId);
        payment.setWebhookReceivedAt(java.time.LocalDateTime.now());

        if (status == PaymentStatus.SUCCEEDED) {
            // Mettre à jour le statut de la commande
            payment.getOrder().setStatus(OrderStatus.PAID);
            orderRepository.save(payment.getOrder());
            log.info("Order {} status updated to PAID", payment.getOrder().getId());
            
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
                // Note: In a real application, you might want to handle this differently
                // For now, we'll log the error but not fail the payment
            }
        }

        payment = paymentRepository.save(payment);
        log.info("Payment status updated successfully");

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
