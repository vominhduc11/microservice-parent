package com.devwonder.cartservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dealer_product_of_carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerProductOfCart {
    
    @Id
    @Column(name = "id_dealer")
    private Long idDealer;
    
    @Id
    @Column(name = "id_product_of_cart") 
    private Long idProductOfCart;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product_of_cart", insertable = false, updatable = false)
    private ProductOfCart productOfCart;
}