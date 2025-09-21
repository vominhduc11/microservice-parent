package com.devwonder.userservice.repository;

import com.devwonder.userservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByAccountId(Long accountId);

    @org.springframework.data.jpa.repository.Query("SELECT MAX(c.accountId) FROM Customer c")
    Long findMaxAccountId();
}