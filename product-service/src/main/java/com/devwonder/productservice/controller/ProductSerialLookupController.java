package com.devwonder.productservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.productservice.service.ProductSerialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-serial")
@Tag(name = "Product Serial Lookup", description = "Product serial lookup endpoints for inter-service communication")
@RequiredArgsConstructor
@Slf4j
public class ProductSerialLookupController {

    private final ProductSerialService productSerialService;

    @GetMapping("/serial/{serial}")
    @Operation(
        summary = "Get Product Serial ID by Serial Number",
        description = "Retrieve the database ID of a product serial by its serial number string. Used by inter-service calls (warranty service). Requires API key authentication.",
        security = @SecurityRequirement(name = "apiKey")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product serial ID retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product serial not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<Long>> getProductSerialIdBySerial(@PathVariable String serial) {

        log.info("Looking up product serial ID for serial: {}", serial);

        Long productSerialId = productSerialService.getProductSerialIdBySerial(serial);

        log.info("Successfully retrieved product serial ID: {} for serial: {}", productSerialId, serial);

        return ResponseEntity.ok(BaseResponse.success("Product serial ID retrieved successfully", productSerialId));
    }
}