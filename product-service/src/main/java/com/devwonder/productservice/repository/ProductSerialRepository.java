package com.devwonder.productservice.repository;

import com.devwonder.productservice.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    boolean existsBySerial(String serial);
}