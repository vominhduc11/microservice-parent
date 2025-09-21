package com.devwonder.productservice.repository;

import com.devwonder.productservice.entity.Product;
import com.devwonder.productservice.entity.ProductSerial;
import com.devwonder.productservice.enums.ProductSerialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    boolean existsBySerial(String serial);
    List<ProductSerial> findByProduct(Product product);

    @Query("SELECT ps.serial FROM ProductSerial ps WHERE ps.product = :product")
    List<String> findSerialsByProduct(@Param("product") Product product);

    @Query("SELECT COUNT(ps) FROM ProductSerial ps WHERE ps.product = :product AND ps.status = :status")
    Long countByProductAndStatus(@Param("product") Product product, @Param("status") ProductSerialStatus status);

    @Query("SELECT COUNT(ps) FROM ProductSerial ps WHERE ps.product = :product")
    Long countByProduct(@Param("product") Product product);

    @Query("SELECT ps FROM ProductSerial ps WHERE ps.product = :product AND ps.status = :status")
    List<ProductSerial> findByProductAndStatus(@Param("product") Product product, @Param("status") ProductSerialStatus status);
}