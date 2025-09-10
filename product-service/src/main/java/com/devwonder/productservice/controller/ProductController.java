package com.devwonder.productservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.productservice.dto.ProductResponse;
import com.devwonder.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@Tag(name = "Product Management", description = "Product management endpoints")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/products/showhomepageandlimit4")
    @Operation(
        summary = "Get Homepage Products",
        description = "Retrieve 4 products to display on homepage with show_on_homepage=true"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getHomepageProducts(
            @RequestParam(required = false) String fields) {
        
        log.info("Requesting homepage products - fields: {}", fields);
        
        List<ProductResponse> products = productService.getHomepageProducts(fields, 4);
        
        log.info("Retrieved {} homepage products", products.size());
        
        return ResponseEntity.ok(BaseResponse.success("Products retrieved successfully", products));
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Product Details",
        description = "Retrieve detailed information about a specific product by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        
        log.info("Requesting product details for ID: {}", id);
        
        ProductResponse product = productService.getProductById(id);
        
        log.info("Retrieved product details for ID: {}", id);
        
        return ResponseEntity.ok(BaseResponse.success("Product details retrieved successfully", product));
    }
    
    @GetMapping("/products/featuredandlimit1")
    @Operation(
        summary = "Get Featured Products",
        description = "Retrieve 1 featured product with is_featured=true"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured products retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getFeaturedProducts(
            @RequestParam(required = false) String fields) {
        
        log.info("Requesting featured products - fields: {}", fields);
        
        List<ProductResponse> products = productService.getFeaturedProducts(fields, 1);
        
        log.info("Retrieved {} featured products", products.size());
        
        return ResponseEntity.ok(BaseResponse.success("Featured products retrieved successfully", products));
    }
}