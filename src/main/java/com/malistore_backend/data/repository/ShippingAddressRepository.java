package com.malistore_backend.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.malistore_backend.data.entity.ShippingAddress;
import com.malistore_backend.data.entity.User;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    
    List<ShippingAddress> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<ShippingAddress> findByIdAndUser(Long id, User user);
    
    Optional<ShippingAddress> findByUserAndIsDefaultTrue(User user);
    
    @Query("SELECT sa FROM ShippingAddress sa WHERE sa.user = :user ORDER BY sa.isDefault DESC, sa.createdAt DESC")
    List<ShippingAddress> findByUserOrderByDefaultAndCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT COUNT(sa) FROM ShippingAddress sa WHERE sa.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT sa FROM ShippingAddress sa WHERE sa.user = :user AND sa.id = :addressId")
    Optional<ShippingAddress> findByUserAndId(@Param("user") User user, @Param("addressId") Long addressId);
}
