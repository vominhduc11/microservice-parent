package com.devwonder.productservice.service;

import com.devwonder.common.exception.ResourceAlreadyExistsException;
import com.devwonder.productservice.dto.ProductSerialCreateRequest;
import com.devwonder.productservice.dto.ProductSerialResponse;
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
}