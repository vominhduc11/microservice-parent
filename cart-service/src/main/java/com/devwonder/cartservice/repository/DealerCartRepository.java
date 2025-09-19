package com.devwonder.cartservice.repository;

import com.devwonder.cartservice.entity.DealerCart;
import com.devwonder.cartservice.entity.DealerCartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerCartRepository extends JpaRepository<DealerCart, DealerCartId> {

    @Query("SELECT dc FROM DealerCart dc WHERE dc.id.idDealer = :dealerId")
    List<DealerCart> findByDealerId(@Param("dealerId") Long dealerId);

}