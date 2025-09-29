package com.devwonder.orderservice.repository;

import com.devwonder.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByIdDealerOrderByCreatedAtDesc(Long idDealer);

    // Find non-deleted orders
    List<Order> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Order> findByIdDealerAndIsDeletedFalseOrderByCreatedAtDesc(Long idDealer);

    List<Order> findByIdDealerAndPaymentStatusAndIsDeletedFalseOrderByCreatedAtDesc(Long idDealer, com.devwonder.orderservice.enums.PaymentStatus paymentStatus);

    List<Order> findByIdDealerAndPaymentStatusOrderByCreatedAtDesc(Long idDealer, com.devwonder.orderservice.enums.PaymentStatus paymentStatus);

    Optional<Order> findByIdAndIsDeletedFalse(Long id);

    // Find deleted orders (for admin)
    List<Order> findByIsDeletedTrueOrderByCreatedAtDesc();

    // Check if order exists and is not deleted
    boolean existsByIdAndIsDeletedFalse(Long id);

    // Count deleted orders
    long countByIsDeletedTrue();

    // Get unique dealer IDs from orders
    @Query("SELECT DISTINCT o.idDealer FROM Order o WHERE o.isDeleted = false")
    List<Long> findDistinctDealerIds();

    // Dashboard queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.isDeleted = false")
    Long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}