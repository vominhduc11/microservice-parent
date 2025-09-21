package com.devwonder.productservice.service;

import com.devwonder.common.exception.ResourceAlreadyExistsException;
import com.devwonder.productservice.dto.ProductSerialCreateRequest;
import com.devwonder.productservice.dto.ProductSerialResponse;
import com.devwonder.productservice.dto.ProductSerialBulkCreateRequest;
import com.devwonder.productservice.dto.ProductSerialBulkCreateResponse;
import com.devwonder.productservice.dto.ProductSerialStatusUpdateRequest;
import com.devwonder.productservice.dto.ProductInventoryResponse;
import com.devwonder.productservice.enums.ProductSerialStatus;
import com.devwonder.productservice.entity.Product;
import com.devwonder.productservice.entity.ProductSerial;
import com.devwonder.productservice.mapper.ProductSerialMapper;
import com.devwonder.productservice.repository.ProductRepository;
import com.devwonder.productservice.repository.ProductSerialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSerialService {
    
    private final ProductSerialRepository productSerialRepository;
    private final ProductRepository productRepository;
    private final ProductSerialMapper productSerialMapper;
    
    @Transactional
    public ProductSerialResponse createProductSerial(ProductSerialCreateRequest request) {
        log.info("Creating new product serial: {}", request.getSerial());
        
        // Check if serial already exists
        if (productSerialRepository.existsBySerial(request.getSerial())) {
            throw new ResourceAlreadyExistsException("Product serial '" + request.getSerial() + "' already exists");
        }
        
        // Check if product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));
        
        ProductSerial productSerial = ProductSerial.builder()
                .serial(request.getSerial())
                .product(product)
                .status(request.getStatus())
                .build();
        
        ProductSerial savedProductSerial = productSerialRepository.save(productSerial);
        log.info("Successfully created product serial with ID: {}", savedProductSerial.getId());
        
        return productSerialMapper.toProductSerialResponse(savedProductSerial);
    }

    public List<ProductSerialResponse> getProductSerialsByProductId(Long productId) {
        log.info("Fetching product serials for product ID: {}", productId);

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        List<ProductSerial> productSerials = productSerialRepository.findByProduct(product);

        return productSerials.stream()
                .map(productSerialMapper::toProductSerialResponse)
                .toList();
    }

    @Transactional
    public ProductSerialBulkCreateResponse createProductSerialsBulk(ProductSerialBulkCreateRequest request) {
        log.info("Creating bulk product serials for product ID: {} with {} serial numbers",
                request.getProductId(), request.getSerialNumbers().size());

        // Check if product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));

        // Get existing serials to avoid duplicates
        Set<String> existingSerials = new HashSet<>(productSerialRepository.findSerialsByProduct(product));

        List<ProductSerial> serialsToCreate = new ArrayList<>();
        List<String> skippedSerials = new ArrayList<>();

        // Process each serial number
        for (String serialNumber : request.getSerialNumbers()) {
            if (existingSerials.contains(serialNumber)) {
                skippedSerials.add(serialNumber);
                log.debug("Skipping duplicate serial: {}", serialNumber);
            } else {
                serialsToCreate.add(ProductSerial.builder()
                        .serial(serialNumber)
                        .product(product)
                        .status(ProductSerialStatus.AVAILABLE)
                        .build());
            }
        }

        // Batch insert new serials
        List<ProductSerial> savedSerials = productSerialRepository.saveAll(serialsToCreate);
        log.info("Successfully created {} product serials, skipped {} duplicates for product ID: {}",
                savedSerials.size(), skippedSerials.size(), request.getProductId());

        // Map to response DTOs
        List<ProductSerialResponse> createdSerials = savedSerials.stream()
                .map(productSerialMapper::toProductSerialResponse)
                .toList();

        return ProductSerialBulkCreateResponse.builder()
                .productId(request.getProductId())
                .totalRequested(request.getSerialNumbers().size())
                .totalCreated(savedSerials.size())
                .totalSkipped(skippedSerials.size())
                .createdSerials(createdSerials)
                .skippedSerials(skippedSerials)
                .build();
    }

    @Transactional
    public void deleteProductSerial(Long serialId) {
        log.info("Deleting product serial with ID: {}", serialId);

        // Check if serial exists
        ProductSerial productSerial = productSerialRepository.findById(serialId)
                .orElseThrow(() -> new RuntimeException("Product serial not found with ID: " + serialId));

        productSerialRepository.delete(productSerial);
        log.info("Successfully deleted product serial with ID: {}", serialId);
    }

    @Transactional
    public ProductSerialResponse updateProductSerialStatus(Long serialId, ProductSerialStatusUpdateRequest request) {
        log.info("Updating status for product serial with ID: {} to {}", serialId, request.getStatus());

        // Check if serial exists
        ProductSerial productSerial = productSerialRepository.findById(serialId)
                .orElseThrow(() -> new RuntimeException("Product serial not found with ID: " + serialId));

        productSerial.setStatus(request.getStatus());
        ProductSerial savedProductSerial = productSerialRepository.save(productSerial);

        log.info("Successfully updated status for product serial with ID: {} to {}", serialId, request.getStatus());

        return productSerialMapper.toProductSerialResponse(savedProductSerial);
    }

    public ProductInventoryResponse getProductInventory(Long productId) {
        log.info("Getting inventory for product ID: {}", productId);

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // Count by status
        Long availableCount = productSerialRepository.countByProductAndStatus(product, ProductSerialStatus.AVAILABLE);
        Long soldCount = productSerialRepository.countByProductAndStatus(product, ProductSerialStatus.SOLD);
        Long damagedCount = productSerialRepository.countByProductAndStatus(product, ProductSerialStatus.DAMAGED);
        Long totalCount = productSerialRepository.countByProduct(product);

        log.info("Inventory for product ID {}: {} available, {} sold, {} damaged, {} total",
                productId, availableCount, soldCount, damagedCount, totalCount);

        return ProductInventoryResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .availableCount(availableCount)
                .soldCount(soldCount)
                .damagedCount(damagedCount)
                .totalCount(totalCount)
                .build();
    }

    public Long getAvailableProductSerialCount(Long productId) {
        log.info("Getting available product serial count for product ID: {}", productId);

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        Long availableCount = productSerialRepository.countByProductAndStatus(product, ProductSerialStatus.AVAILABLE);

        log.info("Available product serial count for product ID {}: {}", productId, availableCount);

        return availableCount;
    }

    @Transactional(readOnly = true)
    public List<ProductSerialResponse> getProductSerialsByProductIdAndStatus(Long productId, ProductSerialStatus status) {
        log.info("Fetching product serials for product ID: {} with status: {}", productId, status);

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        List<ProductSerial> productSerials = productSerialRepository.findByProductAndStatus(product, status);

        List<ProductSerialResponse> responses = productSerials.stream()
                .map(productSerialMapper::toProductSerialResponse)
                .collect(Collectors.toList());

        log.info("Found {} product serials for product ID {} with status {}", responses.size(), productId, status);

        return responses;
    }
}