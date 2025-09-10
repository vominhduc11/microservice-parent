package com.devwonder.userservice.repository;

import com.devwonder.userservice.entity.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
}