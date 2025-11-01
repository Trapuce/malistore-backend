package com.malistore_backend.data.entity;

public enum OrderStatus {
    PENDING,    // Commande en attente de paiement
    PAID,       // Commande payée
    SHIPPED,    // Commande expédiée
    DELIVERED,  // Commande livrée
    CANCELLED   // Commande annulée
}



