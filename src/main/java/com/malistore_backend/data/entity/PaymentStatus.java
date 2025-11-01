package com.malistore_backend.data.entity;

public enum PaymentStatus {
    PENDING,        // Paiement en attente
    PROCESSING,     // Paiement en cours de traitement
    SUCCEEDED,      // Paiement réussi
    FAILED,         // Paiement échoué
    CANCELLED,      // Paiement annulé
    REFUNDED,       // Paiement remboursé
    PARTIALLY_REFUNDED // Paiement partiellement remboursé
}
