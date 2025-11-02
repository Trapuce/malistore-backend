package com.malistore_backend.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.malistore_backend.data.entity.Order;
import com.malistore_backend.data.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrder(Order order);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order = :order ORDER BY oi.createdAt ASC")
    List<OrderItem> findByOrderOrderByCreatedAtAsc(@Param("order") Order order);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order = :order")
    Long countByOrder(@Param("order") Order order);
}




