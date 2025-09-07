package com.devwonder.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dealer_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerOrderItem {
    
    @Id
    @Column(name = "id_dealer")
    private Long idDealer;
    
    @Id
    @Column(name = "id_order_item")
    private Long idOrderItem;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private Order order;
}