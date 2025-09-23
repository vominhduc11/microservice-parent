package com.devwonder.productservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.productservice.dto.ProductSerialCreateRequest;
import com.devwonder.productservice.dto.ProductSerialResponse;
import com.devwonder.productservice.dto.ProductSerialBulkCreateRequest;
import com.devwonder.productservice.dto.ProductSerialBulkCreateResponse;
import com.devwonder.productservice.dto.ProductSerialStatusUpdateRequest;
import com.devwonder.productservice.dto.ProductSerialBulkStatusUpdateRequest;
import com.devwonder.productservice.dto.ProductInventoryResponse;
import com.devwonder.productservice.enums.ProductSerialStatus;
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
@RequestMapping("/product-serials")
@Tag(name = "Product Inventory", description = "📋 Serial number tracking & Inventory management")
@RequiredArgsConstructor
@Slf4j
public class ProductSerialController {
    
    private final ProductSerialService productSerialService;
    
    @PostMapping("/serials")
    @Operation(
        summary = "Create Multiple Product Serials (Bulk)",
        description = "Create multiple product serial numbers in a single request. Requires ADMIN role authentication via API Gateway. Skips duplicate serials and returns detailed results.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product serials created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or empty serial numbers list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductSerialBulkCreateResponse>> createProductSerialsBulk(@Valid @RequestBody ProductSerialBulkCreateRequest request) {

        log.info("Creating bulk product serials: {} serial numbers for product ID: {} by ADMIN user",
                 request.getSerialNumbers().size(), request.getProductId());

        ProductSerialBulkCreateResponse response = productSerialService.createProductSerialsBulk(request);

        log.info("Bulk creation completed: {} created, {} skipped out of {} requested for product ID: {}",
                response.getTotalCreated(), response.getTotalSkipped(), response.getTotalRequested(), response.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Product serials bulk creation completed", response));
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

        log.info("Requesting product serials for product ID: {} by authorized user", productId);

        List<ProductSerialResponse> productSerials = productSerialService.getProductSerialsByProductId(productId);

        log.info("Retrieved {} product serials for product ID: {}", productSerials.size(), productId);

        return ResponseEntity.ok(BaseResponse.success("Product serials retrieved successfully", productSerials));
    }

    @PostMapping("/serial")
    @Operation(
        summary = "Create Single Product Serial",
        description = "Create a single product serial number. Requires ADMIN role authentication via API Gateway.",
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
    public ResponseEntity<BaseResponse<ProductSerialResponse>> createSingleProductSerial(@Valid @RequestBody ProductSerialCreateRequest request) {

        log.info("Creating single product serial: {} for product ID: {} by ADMIN user", request.getSerial(), request.getProductId());

        ProductSerialResponse productSerial = productSerialService.createProductSerial(request);

        log.info("Successfully created single product serial with ID: {}", productSerial.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Product serial created successfully", productSerial));
    }

    @DeleteMapping("/serial/{serialId}")
    @Operation(
        summary = "Delete Product Serial",
        description = "Delete a specific product serial by ID. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product serial deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product serial not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Void>> deleteProductSerial(@PathVariable Long serialId) {

        log.info("Deleting product serial with ID: {} by ADMIN user", serialId);

        productSerialService.deleteProductSerial(serialId);

        log.info("Successfully deleted product serial with ID: {}", serialId);

        return ResponseEntity.ok(BaseResponse.success("Product serial deleted successfully", null));
    }

    @PatchMapping("/serial/{serialId}/status")
    @Operation(
        summary = "Update Product Serial Status",
        description = "Update status of a specific product serial (AVAILABLE/SOLD_TO_DEALER/SOLD_TO_CUSTOMER/DAMAGED). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product serial status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product serial not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductSerialResponse>> updateProductSerialStatus(
            @PathVariable Long serialId,
            @Valid @RequestBody ProductSerialStatusUpdateRequest request) {

        log.info("Updating status for product serial with ID: {} to {} by ADMIN user", serialId, request.getStatus());

        ProductSerialResponse updatedSerial = productSerialService.updateProductSerialStatus(serialId, request);

        log.info("Successfully updated status for product serial with ID: {} to {}", serialId, request.getStatus());

        return ResponseEntity.ok(BaseResponse.success("Product serial status updated successfully", updatedSerial));
    }

    @GetMapping("/{productId}/inventory")
    @Operation(
        summary = "Get Product Inventory Count",
        description = "Get detailed inventory count for a product by status (AVAILABLE/SOLD_TO_DEALER/SOLD_TO_CUSTOMER/DAMAGED). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product inventory retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductInventoryResponse>> getProductInventory(@PathVariable Long productId) {

        log.info("Getting inventory for product ID: {} by ADMIN user", productId);

        ProductInventoryResponse inventory = productSerialService.getProductInventory(productId);

        log.info("Retrieved inventory for product ID: {} - {} available out of {} total",
                productId, inventory.getAvailableCount(), inventory.getTotalCount());

        return ResponseEntity.ok(BaseResponse.success("Product inventory retrieved successfully", inventory));
    }

    @GetMapping("/{productId}/available-count")
    @Operation(
        summary = "Get Available Product Serial Count",
        description = "Get count of available product serials for a specific product. Requires DEALER role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Long>> getAvailableProductSerialCount(@PathVariable Long productId) {

        log.info("Getting available product serial count for product ID: {} by DEALER user", productId);

        Long availableCount = productSerialService.getAvailableProductSerialCount(productId);

        log.info("Retrieved available count for product ID: {} - {} available", productId, availableCount);

        return ResponseEntity.ok(BaseResponse.success("Available product serial count retrieved successfully", availableCount));
    }

    @GetMapping("/{productId}/serials/status/{status}")
    @Operation(
        summary = "Get Product Serials by Status",
        description = "Retrieve all product serials for a specific product filtered by status (AVAILABLE/SOLD_TO_DEALER/SOLD_TO_CUSTOMER/DAMAGED). Requires ADMIN or DEALER role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product serials retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN or DEALER role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductSerialResponse>>> getProductSerialsByStatus(
            @PathVariable Long productId,
            @PathVariable ProductSerialStatus status) {

        log.info("Requesting product serials for product ID: {} with status: {} by authorized user", productId, status);

        List<ProductSerialResponse> productSerials = productSerialService.getProductSerialsByProductIdAndStatus(productId, status);

        log.info("Retrieved {} product serials for product ID: {} with status: {}", productSerials.size(), productId, status);

        return ResponseEntity.ok(BaseResponse.success("Product serials retrieved successfully", productSerials));
    }


}