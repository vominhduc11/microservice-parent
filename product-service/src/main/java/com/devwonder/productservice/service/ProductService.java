package com.devwonder.productservice.service;

import com.devwonder.productservice.dto.ProductResponse;
import com.devwonder.productservice.entity.Product;
import com.devwonder.productservice.mapper.ProductMapper;
import com.devwonder.productservice.repository.ProductRepository;
import com.devwonder.productservice.util.FieldFilterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FieldFilterUtil fieldFilterUtil;
    
    public List<ProductResponse> getHomepageProducts(String fields, int limit) {
        log.info("Fetching homepage products with fields: {}, limit: {}", fields, limit);
        
        List<Product> products = productRepository.findByShowOnHomepageTrue();
        
        return products.stream()
                .limit(limit)
                .map(product -> fieldFilterUtil.applyFieldFiltering(productMapper.toProductResponse(product), fields))
                .toList();
    }
    
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product details for ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        return productMapper.toProductResponse(product);
    }
    
    public List<ProductResponse> getFeaturedProducts(String fields, int limit) {
        log.info("Fetching featured products with fields: {}, limit: {}", fields, limit);
        
        List<Product> products = productRepository.findByIsFeaturedTrue();
        
        return products.stream()
                .limit(limit)
                .map(product -> fieldFilterUtil.applyFieldFiltering(productMapper.toProductResponse(product), fields))
                .toList();
    }
}