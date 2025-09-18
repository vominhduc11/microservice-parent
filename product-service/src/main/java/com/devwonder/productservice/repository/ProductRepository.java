package com.devwonder.productservice.repository;

import com.devwonder.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByShowOnHomepageTrueAndIsDeletedFalse();

    List<Product> findByIsFeaturedTrueAndIsDeletedFalse();

    List<Product> findByIsDeletedFalse();

    List<Product> findByIsDeletedTrue();

    List<Product> findByIsDeletedFalseAndIdNot(Long id);

    boolean existsBySkuAndIsDeletedFalse(String sku);
}