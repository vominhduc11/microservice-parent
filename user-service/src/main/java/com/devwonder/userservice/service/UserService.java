package com.devwonder.userservice.service;

import com.devwonder.common.exception.AccountCreationException;
import com.devwonder.common.exception.ResourceAlreadyExistsException;
import com.devwonder.userservice.client.AuthServiceClient;
import com.devwonder.userservice.dto.*;
import com.devwonder.userservice.entity.Dealer;
import com.devwonder.userservice.mapper.DealerMapper;
import com.devwonder.userservice.repository.DealerRepository;
import com.devwonder.userservice.util.AccountGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;
    private final AuthServiceClient authServiceClient;
    private final DealerEventService dealerEventService;

    @Transactional(readOnly = true)
    public List<DealerResponse> getAllDealers() {
        log.info("Fetching all dealers from database");
        
        List<Dealer> dealers = dealerRepository.findAll();
        
        log.info("Found {} dealers in system", dealers.size());
        
        return dealers.stream()
                .map(dealerMapper::toResponse)
                .toList();
    }

    @Transactional
    public DealerResponse createDealer(DealerRequest dealerRequest) {
        log.info("Creating new dealer for company: {}", dealerRequest.getCompanyName());
        
        // Check if phone already exists
        if (dealerRepository.existsByPhone(dealerRequest.getPhone())) {
            log.warn("Dealer with phone {} already exists", dealerRequest.getPhone());
            throw new ResourceAlreadyExistsException("Dealer with phone " + dealerRequest.getPhone() + " already exists");
        }
        
        // Check if email already exists
        if (dealerRepository.existsByEmail(dealerRequest.getEmail())) {
            log.warn("Dealer with email {} already exists", dealerRequest.getEmail());
            throw new ResourceAlreadyExistsException("Dealer with email " + dealerRequest.getEmail() + " already exists");
        }
        
        // Generate username and password automatically
        String username = AccountGeneratorUtil.generateUsername(dealerRequest.getCompanyName());
        String password = AccountGeneratorUtil.generateDealerPassword();
        
        log.info("Generated username: {} for dealer", username);
        
        // Create account in auth-service
        AuthAccountCreateRequest authRequest = AuthAccountCreateRequest.builder()
                .username(username)
                .password(password)
                .roleNames(Set.of("DEALER"))
                .build();
        
        try {
            var authResponse = authServiceClient.createAccount(authRequest, "user-service");
            log.info("Successfully created account with ID: {} for dealer", authResponse.getData().getId());
            
            // Map request to entity
            Dealer dealer = dealerMapper.toEntity(dealerRequest);
            dealer.setAccountId(authResponse.getData().getId());
            
            // Save dealer
            Dealer savedDealer = dealerRepository.save(dealer);
            log.info("Successfully created dealer with accountId: {}", savedDealer.getAccountId());
            
            // Publish dealer events to Kafka (email và socket riêng biệt)
            dealerEventService.publishDealerEmailEvent(savedDealer, username, password);
            dealerEventService.publishDealerSocketEvent(savedDealer);
            
            // Return response
            return dealerMapper.toResponse(savedDealer);
            
        } catch (Exception e) {
            log.error("Failed to create account for dealer: {}", e.getMessage());
            throw new AccountCreationException("Failed to create dealer account: " + e.getMessage(), e);
        }
    }
}