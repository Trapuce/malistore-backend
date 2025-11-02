package com.malistore_backend.data.repository;

import com.malistore_backend.data.entity.Order;
import com.malistore_backend.data.entity.Payment;
import com.malistore_backend.data.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Trouve tous les paiements d'une commande
     */
    List<Payment> findByOrderOrderByCreatedAtDesc(Order order);
    
    /**
     * Trouve un paiement par son ID de transaction (Stripe)
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Trouve un paiement par son ID de session Stripe
     */
    Optional<Payment> findByStripeSessionId(String stripeSessionId);
    
    /**
     * Trouve un paiement par son ID de payment intent Stripe
     */
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    /**
     * Trouve les paiements par statut
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
    
    /**
     * Trouve les paiements d'un utilisateur via sa commande
     */
    @Query("SELECT p FROM Payment p JOIN p.order o WHERE o.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);
    
    /**
     * Trouve les paiements réussis d'un utilisateur
     */
    @Query("SELECT p FROM Payment p JOIN p.order o WHERE o.user.id = :userId AND p.status = 'SUCCEEDED' ORDER BY p.createdAt DESC")
    List<Payment> findSuccessfulPaymentsByUserId(@Param("userId") Long userId);
    
    /**
     * Trouve le dernier paiement d'une commande
     */
    @Query("SELECT p FROM Payment p WHERE p.order = :order ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Payment> findLatestPaymentByOrder(@Param("order") Order order);
    
    /**
     * Vérifie si une commande a un paiement réussi
     */
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.order = :order AND p.status = 'SUCCEEDED'")
    boolean hasSuccessfulPayment(@Param("order") Order order);
}




