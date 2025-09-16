package com.devwonder.productservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.productservice.dto.ProductSerialCreateRequest;
import com.devwonder.productservice.dto.ProductSerialResponse;
import com.devwonder.productservice.service.ProductSerialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@Tag(name = "Product Serial Management", description = "Product serial management endpoints")
@RequiredArgsConstructor
@Slf4j
public class ProductSerialController {
    
    private final ProductSerialService productSerialService;
    
    @PostMapping("/serials")
    @Operation(
        summary = "Create New Product Serial",
        description = "Create a new product serial number. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product serial created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product serial data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - Product serial already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductSerialResponse>> createProductSerial(@Valid @RequestBody ProductSerialCreateRequest request) {
        
        log.info("Creating new product serial: {} for product ID: {} by ADMIN user", request.getSerial(), request.getProductId());
        
        ProductSerialResponse productSerial = productSerialService.createProductSerial(request);
        
        log.info("Successfully created product serial with ID: {}", productSerial.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Product serial created successfully", productSerial));
    }

    @GetMapping("/{productId}/serials")
    @Operation(
        summary = "Get Product Serials by Product ID",
        description = "Retrieve all product serials for a specific product. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product serials retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductSerialResponse>>> getProductSerialsByProductId(@PathVariable Long productId) {

        log.info("Requesting product serials for product ID: {} by ADMIN user", productId);

        List<ProductSerialResponse> productSerials = productSerialService.getProductSerialsByProductId(productId);

        log.info("Retrieved {} product serials for product ID: {}", productSerials.size(), productId);

        return ResponseEntity.ok(BaseResponse.success("Product serials retrieved successfully", productSerials));
    }
}