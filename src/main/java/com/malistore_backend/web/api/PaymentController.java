package com.malistore_backend.web.api;

import com.malistore_backend.data.entity.PaymentStatus;
import com.malistore_backend.service.MockPaymentService;
import com.malistore_backend.service.StripeWebhookService;
import com.malistore_backend.web.dto.payment.PaymentResponse;
import com.malistore_backend.web.dto.payment.PaymentSessionRequest;
import com.malistore_backend.web.dto.payment.PaymentSessionResponse;
import com.malistore_backend.web.payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.web.exception.ResourceNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final MockPaymentService mockPaymentService;
    private final StripeWebhookService stripeWebhookService;
    private final UserRepository userRepository;

    /**
     * Crée une session de paiement pour une commande
     */
    @PostMapping("/create-session")
    public ResponseEntity<ApiResponse<PaymentSessionResponse>> createPaymentSession(
            @Valid @RequestBody PaymentSessionRequest request,
            Authentication authentication) {
        
        // User authentication handled by Spring Security
        PaymentSessionResponse response = mockPaymentService.createPaymentSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Récupère les paiements de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getUserPayments(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<PaymentResponse> payments = mockPaymentService.getUserPayments(user.getId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Récupère un paiement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = mockPaymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    /**
     * Récupère les paiements d'une commande
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getOrderPayments(@PathVariable Long orderId) {
        List<PaymentResponse> payments = mockPaymentService.getOrderPayments(orderId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Récupère les paiements par statut (admin)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponse> payments = mockPaymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Simule un paiement réussi (pour les tests)
     */
    @PostMapping("/simulate-success/{sessionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> simulateSuccessfulPayment(@PathVariable String sessionId) {
        PaymentResponse payment = mockPaymentService.simulateSuccessfulPayment(sessionId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    /**
     * Simule un paiement échoué (pour les tests)
     */
    @PostMapping("/simulate-failure/{sessionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> simulateFailedPayment(@PathVariable String sessionId) {
        PaymentResponse payment = mockPaymentService.simulateFailedPayment(sessionId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    /**
     * Webhook Stripe pour les notifications de paiement
     */
    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        try {
            stripeWebhookService.handleWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
}
