package com.devwonder.productservice.repository;

import com.devwonder.productservice.entity.Product;
import com.devwonder.productservice.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    boolean existsBySerial(String serial);
    List<ProductSerial> findByProduct(Product product);
}