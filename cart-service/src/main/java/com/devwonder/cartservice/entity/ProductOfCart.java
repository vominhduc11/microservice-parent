package com.devwonder.cartservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_of_carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOfCart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "id_product", nullable = false)
    private Long idProduct;
    
    @OneToMany(mappedBy = "productOfCart", cascade = CascadeType.ALL)
    private List<DealerProductOfCart> dealerProductOfCarts;
}