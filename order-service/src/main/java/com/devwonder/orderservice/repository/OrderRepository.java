package com.devwonder.orderservice.repository;

import com.devwonder.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreateAtDesc();

    List<Order> findByIdDealerOrderByCreateAtDesc(Long idDealer);
}