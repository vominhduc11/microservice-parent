package com.devwonder.productservice.service;

import com.devwonder.common.exception.ResourceAlreadyExistsException;
import com.devwonder.productservice.dto.ProductCreateRequest;
import com.devwonder.productservice.dto.ProductResponse;
import com.devwonder.productservice.dto.ProductUpdateRequest;
import com.devwonder.productservice.entity.Product;
import com.devwonder.productservice.mapper.ProductMapper;
import com.devwonder.productservice.repository.ProductRepository;
import com.devwonder.productservice.util.FieldFilterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());
        
        // Check if SKU already exists
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .image(request.getImage())
                .description(request.getDescription())
                .videos(request.getVideos())
                .specifications(request.getSpecifications())
                .retailPrice(request.getRetailPrice())
                .wholesalePrice(request.getWholesalePrice())
                .status(request.getStatus())
                .soldQuantity(0L)
                .showOnHomepage(request.getShowOnHomepage())
                .isFeatured(request.getIsFeatured())
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        
        return productMapper.toProductResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("Updating product with ID: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        // Check if SKU is being updated and if new SKU already exists
        if (request.getSku() != null && !request.getSku().equals(existingProduct.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new ResourceAlreadyExistsException("Product with SKU '" + request.getSku() + "' already exists");
            }
            existingProduct.setSku(request.getSku());
        }
        
        // Update only non-null fields (PATCH behavior)
        if (request.getName() != null) {
            existingProduct.setName(request.getName());
        }
        if (request.getImage() != null) {
            existingProduct.setImage(request.getImage());
        }
        if (request.getDescription() != null) {
            existingProduct.setDescription(request.getDescription());
        }
        if (request.getVideos() != null) {
            existingProduct.setVideos(request.getVideos());
        }
        if (request.getSpecifications() != null) {
            existingProduct.setSpecifications(request.getSpecifications());
        }
        if (request.getRetailPrice() != null) {
            existingProduct.setRetailPrice(request.getRetailPrice());
        }
        if (request.getWholesalePrice() != null) {
            existingProduct.setWholesalePrice(request.getWholesalePrice());
        }
        if (request.getStatus() != null) {
            existingProduct.setStatus(request.getStatus());
        }
        if (request.getShowOnHomepage() != null) {
            existingProduct.setShowOnHomepage(request.getShowOnHomepage());
        }
        if (request.getIsFeatured() != null) {
            existingProduct.setIsFeatured(request.getIsFeatured());
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Successfully updated product with ID: {} and SKU: {}", updatedProduct.getId(), updatedProduct.getSku());
        
        return productMapper.toProductResponse(updatedProduct);
    }
}