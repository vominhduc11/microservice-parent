package com.devwonder.userservice.service;

import com.devwonder.common.exception.AccountCreationException;
import com.devwonder.common.exception.ResourceAlreadyExistsException;
import com.devwonder.userservice.client.AuthServiceClient;
import com.devwonder.userservice.dto.*;
import com.devwonder.common.exception.ResourceNotFoundException;
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
    private final com.devwonder.userservice.util.FieldFilterUtil fieldFilterUtil;

    @Transactional(readOnly = true)
    public List<DealerResponse> getAllDealers() {
        log.info("Fetching all dealers from database");

        List<Dealer> dealers = dealerRepository.findAll();

        log.info("Found {} dealers in system", dealers.size());

        return dealers.stream()
                .map(dealerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DealerResponse getDealerById(Long dealerId, String fields) {
        log.info("Fetching dealer with ID: {} - fields: {}", dealerId, fields);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with ID: " + dealerId));

        log.info("Found dealer: {} with accountId: {}", dealer.getCompanyName(), dealer.getAccountId());

        DealerResponse response = dealerMapper.toResponse(dealer);
        return fieldFilterUtil.applyFieldFiltering(response, fields);
    }

    @Transactional(readOnly = true)
    public DealerResponse getDealerById(Long dealerId) {
        return getDealerById(dealerId, null);
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
            var authResponse = authServiceClient.createAccount(authRequest, "INTER_SERVICE_KEY");
            log.info("Successfully created account with ID: {} for dealer", authResponse.getData().getId());
            
            // Map request to entity
            Dealer dealer = dealerMapper.toEntity(dealerRequest);
            dealer.setAccountId(authResponse.getData().getId());
            
            // Save dealer
            Dealer savedDealer = dealerRepository.save(dealer);
            log.info("Successfully created dealer with accountId: {}", savedDealer.getAccountId());
            
            // Publish dealer events to Kafka (email and socket notifications)
            dealerEventService.publishDealerEmailEvent(savedDealer, username, password);
            dealerEventService.publishDealerRegistrationEvent(savedDealer);
            
            // Return response
            return dealerMapper.toResponse(savedDealer);
            
        } catch (Exception e) {
            log.error("Failed to create account for dealer: {}", e.getMessage());
            throw new AccountCreationException("Failed to create dealer account: " + e.getMessage(), e);
        }
    }

    @Transactional
    public DealerResponse updateDealer(Long dealerId, DealerUpdateRequest updateRequest) {
        log.info("Updating dealer with ID: {}", dealerId);
        
        // Find existing dealer
        Dealer existingDealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with ID: " + dealerId));
        
        // Check if phone already exists for another dealer
        if (!updateRequest.getPhone().equals(existingDealer.getPhone()) &&
            dealerRepository.existsByPhone(updateRequest.getPhone())) {
            log.warn("Phone {} already exists for another dealer", updateRequest.getPhone());
            throw new ResourceAlreadyExistsException("Phone " + updateRequest.getPhone() + " already exists for another dealer");
        }
        
        // Check if email already exists for another dealer
        if (!updateRequest.getEmail().equals(existingDealer.getEmail()) &&
            dealerRepository.existsByEmail(updateRequest.getEmail())) {
            log.warn("Email {} already exists for another dealer", updateRequest.getEmail());
            throw new ResourceAlreadyExistsException("Email " + updateRequest.getEmail() + " already exists for another dealer");
        }
        
        // Full replacement - update ALL fields (PUT semantics)
        existingDealer.setCompanyName(updateRequest.getCompanyName());
        existingDealer.setAddress(updateRequest.getAddress());
        existingDealer.setPhone(updateRequest.getPhone());
        existingDealer.setEmail(updateRequest.getEmail());
        existingDealer.setDistrict(updateRequest.getDistrict());
        existingDealer.setCity(updateRequest.getCity());
        
        // Save updated dealer
        Dealer updatedDealer = dealerRepository.save(existingDealer);
        log.info("Successfully updated dealer with accountId: {}", updatedDealer.getAccountId());
        
        return dealerMapper.toResponse(updatedDealer);
    }

    @Transactional
    public void deleteDealer(Long dealerId) {
        log.info("Deleting dealer with ID: {}", dealerId);
        
        // Find existing dealer first to get accountId
        Dealer existingDealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with ID: " + dealerId));
        
        Long accountId = existingDealer.getAccountId();
        log.info("Found dealer with accountId: {}, proceeding to delete both dealer and account", accountId);
        
        try {
            // Delete dealer first (local transaction)
            dealerRepository.deleteById(dealerId);
            log.info("Successfully deleted dealer with ID: {}", dealerId);
            
            // Delete corresponding account in auth-service
            authServiceClient.deleteAccount(accountId, "INTER_SERVICE_KEY");
            log.info("Successfully deleted account with ID: {} in auth-service", accountId);
            
        } catch (Exception e) {
            log.error("Failed to delete dealer or account: {}", e.getMessage());
            throw new RuntimeException("Failed to delete dealer and associated account: " + e.getMessage(), e);
        }
    }


}