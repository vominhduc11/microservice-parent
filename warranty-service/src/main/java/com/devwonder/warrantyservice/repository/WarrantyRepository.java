package com.devwonder.warrantyservice.repository;

import com.devwonder.warrantyservice.entity.Warranty;
import com.devwonder.warrantyservice.enums.WarrantyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {

    List<Warranty> findByIdCustomer(Long idCustomer);

    List<Warranty> findByIdProductSerial(Long idProductSerial);

    @Query("SELECT w FROM Warranty w WHERE w.idProductSerial = :idProductSerial AND w.status = 'ACTIVE'")
    Optional<Warranty> findActiveWarrantyByProductSerial(@Param("idProductSerial") Long idProductSerial);

    @Query("SELECT w FROM Warranty w WHERE w.idProductSerial = :idProductSerial AND w.idCustomer = :idCustomer AND w.status = :status")
    Optional<Warranty> findByIdProductSerialAndIdCustomerAndStatus(
            @Param("idProductSerial") Long idProductSerial,
            @Param("idCustomer") Long idCustomer,
            @Param("status") WarrantyStatus status);
}