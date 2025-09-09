package com.devwonder.userservice.service;

import com.devwonder.userservice.dto.DealerResponse;
import com.devwonder.userservice.entity.Dealer;
import com.devwonder.userservice.mapper.DealerMapper;
import com.devwonder.userservice.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;

    @Transactional(readOnly = true)
    public List<DealerResponse> getAllDealers() {
        log.info("Fetching all dealers from database");
        
        List<Dealer> dealers = dealerRepository.findAll();
        
        log.info("Found {} dealers in system", dealers.size());
        
        return dealers.stream()
                .map(dealerMapper::toResponse)
                .toList();
    }
}