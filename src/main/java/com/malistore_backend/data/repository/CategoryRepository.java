package com.malistore_backend.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.malistore_backend.data.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Trouve une catégorie par son nom (insensible à la casse)
     */
    Optional<Category> findByNameIgnoreCase(String name);
    
    /**
     * Vérifie si une catégorie existe par son nom (insensible à la casse)
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Trouve toutes les catégories actives
     */
    List<Category> findByActiveTrue();
    
    /**
     * Trouve toutes les catégories actives triées par ordre de tri
     */
    List<Category> findByActiveTrueOrderBySortOrderAsc();
    
    /**
     * Trouve toutes les catégories triées par ordre de tri
     */
    List<Category> findAllByOrderBySortOrderAsc();
    
    /**
     * Trouve les catégories par nom contenant le terme de recherche (insensible à la casse)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    /**
     * Compte le nombre de produits dans une catégorie
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}
