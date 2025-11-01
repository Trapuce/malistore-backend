package com.malistore_backend.data.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.malistore_backend.data.entity.CartItem;
import com.malistore_backend.data.entity.Product;
import com.malistore_backend.data.entity.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Trouve tous les articles du panier d'un utilisateur
     */
    List<CartItem> findByUser(User user);
    
    /**
     * Trouve tous les articles du panier d'un utilisateur triés par date de création
     */
    List<CartItem> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Trouve un article spécifique dans le panier d'un utilisateur
     */
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    
    /**
     * Trouve un article par son ID et l'utilisateur (sécurité)
     */
    Optional<CartItem> findByIdAndUser(Long id, User user);
    
    /**
     * Supprime tous les articles du panier d'un utilisateur
     */
    void deleteByUser(User user);
    
    /**
     * Compte le nombre d'articles dans le panier d'un utilisateur
     */
    Long countByUser(User user);
    
    /**
     * Calcule le total du panier d'un utilisateur
     */
    @Query("SELECT COALESCE(SUM(ci.totalPrice), 0) FROM CartItem ci WHERE ci.user = :user")
    BigDecimal calculateCartTotal(@Param("user") User user);
    
    /**
     * Trouve les articles du panier avec les détails des produits
     */
    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.product p " +
           "JOIN FETCH p.category " +
           "WHERE ci.user = :user " +
           "ORDER BY ci.createdAt DESC")
    List<CartItem> findByUserWithProductDetails(@Param("user") User user);
    
    /**
     * Vérifie si un produit existe déjà dans le panier d'un utilisateur
     */
    boolean existsByUserAndProduct(User user, Product product);
    
    /**
     * Trouve les articles du panier d'un utilisateur avec une quantité spécifique
     */
    List<CartItem> findByUserAndQuantityGreaterThan(User user, Integer quantity);
}
