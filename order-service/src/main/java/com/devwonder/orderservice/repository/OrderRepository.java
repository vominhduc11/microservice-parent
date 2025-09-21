package com.devwonder.orderservice.repository;

import com.devwonder.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreateAtDesc();

    List<Order> findByIdDealerOrderByCreateAtDesc(Long idDealer);

    // Find non-deleted orders
    List<Order> findByIsDeletedFalseOrderByCreateAtDesc();

    List<Order> findByIdDealerAndIsDeletedFalseOrderByCreateAtDesc(Long idDealer);

    Optional<Order> findByIdAndIsDeletedFalse(Long id);

    // Find deleted orders (for admin)
    List<Order> findByIsDeletedTrueOrderByCreateAtDesc();

    // Check if order exists and is not deleted
    boolean existsByIdAndIsDeletedFalse(Long id);

    // Count deleted orders
    long countByIsDeletedTrue();
}