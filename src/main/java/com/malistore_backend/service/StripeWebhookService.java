package com.malistore_backend.service;

import com.malistore_backend.data.entity.PaymentStatus;
import com.malistore_backend.data.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Traite un webhook Stripe avec vérification de signature
     */
    @Transactional
    public void handleWebhook(String payload, String signature) {
        log.info("Processing Stripe webhook with signature verification");

        try {
            // Vérifier la signature du webhook
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            log.info("Webhook event type: {}", event.getType());

            // Traiter l'événement selon son type
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }

        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            throw new RuntimeException("Invalid webhook signature", e);
        } catch (StripeException e) {
            log.error("Stripe error processing webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe error", e);
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    /**
     * Traite l'événement checkout.session.completed
     */
    private void handleCheckoutSessionCompleted(Event event) {
        log.info("Processing checkout.session.completed event");
        
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                log.error("Session object is null");
                return;
            }

            String sessionId = session.getId();
            log.info("Checkout session completed: {}", sessionId);

            // Mettre à jour le statut du paiement
            paymentService.updatePaymentStatus(sessionId, PaymentStatus.SUCCEEDED, session.getPaymentIntent());

        } catch (Exception e) {
            log.error("Error handling checkout session completed: {}", e.getMessage(), e);
        }
    }

    /**
     * Traite l'événement payment_intent.succeeded
     */
    private void handlePaymentIntentSucceeded(Event event) {
        log.info("Processing payment_intent.succeeded event");
        
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent == null) {
                log.error("PaymentIntent object is null");
                return;
            }

            String paymentIntentId = paymentIntent.getId();
            log.info("Payment intent succeeded: {}", paymentIntentId);

            // Trouver le paiement par payment intent ID
            paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .ifPresentOrElse(
                            payment -> {
                                payment.setStatus(PaymentStatus.SUCCEEDED);
                                payment.setWebhookReceivedAt(java.time.LocalDateTime.now());
                                paymentRepository.save(payment);
                                log.info("Payment status updated to SUCCEEDED for payment intent: {}", paymentIntentId);
                            },
                            () -> log.warn("No payment found for payment intent: {}", paymentIntentId)
                    );

        } catch (Exception e) {
            log.error("Error handling payment intent succeeded: {}", e.getMessage(), e);
        }
    }

    /**
     * Traite l'événement payment_intent.payment_failed
     */
    private void handlePaymentIntentFailed(Event event) {
        log.info("Processing payment_intent.payment_failed event");
        
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent == null) {
                log.error("PaymentIntent object is null");
                return;
            }

            String paymentIntentId = paymentIntent.getId();
            String failureReason = paymentIntent.getLastPaymentError() != null ? 
                    paymentIntent.getLastPaymentError().getMessage() : "Payment failed";

            log.info("Payment intent failed: {} - {}", paymentIntentId, failureReason);

            // Trouver le paiement par payment intent ID
            paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .ifPresentOrElse(
                            payment -> {
                                payment.setStatus(PaymentStatus.FAILED);
                                payment.setFailureReason(failureReason);
                                payment.setWebhookReceivedAt(java.time.LocalDateTime.now());
                                paymentRepository.save(payment);
                                log.info("Payment status updated to FAILED for payment intent: {}", paymentIntentId);
                            },
                            () -> log.warn("No payment found for payment intent: {}", paymentIntentId)
                    );

        } catch (Exception e) {
            log.error("Error handling payment intent failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Traite l'événement payment_intent.canceled
     */
    private void handlePaymentIntentCanceled(Event event) {
        log.info("Processing payment_intent.canceled event");
        
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent == null) {
                log.error("PaymentIntent object is null");
                return;
            }

            String paymentIntentId = paymentIntent.getId();
            log.info("Payment intent canceled: {}", paymentIntentId);

            // Trouver le paiement par payment intent ID
            paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .ifPresentOrElse(
                            payment -> {
                                payment.setStatus(PaymentStatus.CANCELLED);
                                payment.setWebhookReceivedAt(java.time.LocalDateTime.now());
                                paymentRepository.save(payment);
                                log.info("Payment status updated to CANCELLED for payment intent: {}", paymentIntentId);
                            },
                            () -> log.warn("No payment found for payment intent: {}", paymentIntentId)
                    );

        } catch (Exception e) {
            log.error("Error handling payment intent canceled: {}", e.getMessage(), e);
        }
    }
}



