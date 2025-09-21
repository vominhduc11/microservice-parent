package com.devwonder.warrantyservice.entity;

import com.devwonder.warrantyservice.enums.WarrantyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warranties", indexes = {
    @Index(name = "idx_warranty_product_serial", columnList = "id_product_serial"),
    @Index(name = "idx_warranty_customer", columnList = "id_customer"),
    @Index(name = "idx_warranty_code", columnList = "warranty_code"),
    @Index(name = "idx_warranty_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "id_product_serial", nullable = false)
    private Long idProductSerial;

    @NotNull
    @Column(name = "id_customer", nullable = false)
    private Long idCustomer;

    @NotBlank
    @Column(name = "warranty_code", unique = true, nullable = false, length = 50)
    private String warrantyCode;

    @Column(name = "warranty_period")
    private Integer warrantyPeriod;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WarrantyStatus status = WarrantyStatus.ACTIVE;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;
}