package com.devwonder.productservice.entity;

import com.devwonder.productservice.enums.ProductSerialStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_serials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSerial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_serial_seq")
    @SequenceGenerator(name = "product_serial_seq", sequenceName = "product_serial_id_seq", allocationSize = 50)
    private Long id;


    @Column(unique = true, nullable = false)
    private String serial;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductSerialStatus status = ProductSerialStatus.AVAILABLE;

}