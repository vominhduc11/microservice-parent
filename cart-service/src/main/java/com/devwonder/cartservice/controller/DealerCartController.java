package com.devwonder.cartservice.controller;

import com.devwonder.cartservice.dto.AddToCartRequest;
import com.devwonder.cartservice.dto.CartResponse;
import com.devwonder.cartservice.service.DealerCartService;
import com.devwonder.common.dto.BaseResponse;
import com.devwonder.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/cart")
@Tag(name = "Dealer Cart Management", description = "Dealer cart management endpoints")
@RequiredArgsConstructor
@Slf4j
public class DealerCartController {

    private final DealerCartService dealerCartService;

    @PostMapping("/add")
    @Operation(summary = "Add Product to Dealer Cart",
               description = "Add a product to dealer's cart. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<CartResponse>> addProductToCart(
            @Parameter(description = "Product and dealer information", required = true)
            @Valid @RequestBody AddToCartRequest request) {

        log.info("Received add to cart request - dealer: {}, product: {}, quantity: {}",
                request.getDealerId(), request.getProductId(), request.getQuantity());

        try {
            CartResponse cartResponse = dealerCartService.addProductToCart(request);
            return ResponseEntity.ok(BaseResponse.success("Product added to cart successfully", cartResponse));

        } catch (Exception e) {
            log.error("Failed to add product to cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to add product to cart: " + e.getMessage()));
        }
    }

    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Get Dealer Cart",
               description = "Retrieve all products in a dealer's cart. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<CartResponse>> getDealerCart(
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId) {

        log.info("Received get cart request for dealer: {}", dealerId);

        try {
            CartResponse cartResponse = dealerCartService.getDealerCart(dealerId);
            return ResponseEntity.ok(BaseResponse.success("Cart retrieved successfully", cartResponse));

        } catch (Exception e) {
            log.error("Failed to retrieve dealer cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to retrieve cart: " + e.getMessage()));
        }
    }

    @DeleteMapping("/dealer/{dealerId}/product/{productId}")
    @Operation(summary = "Remove Product from Cart",
               description = "Remove a product from dealer's cart. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed from cart successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found in cart"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<String>> removeProductFromCart(
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {

        log.info("Received remove from cart request - dealer: {}, product: {}", dealerId, productId);

        try {
            dealerCartService.removeProductFromCart(dealerId, productId);
            return ResponseEntity.ok(BaseResponse.success("Product removed from cart successfully", null));

        } catch (ResourceNotFoundException e) {
            log.error("Product not found in cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to remove product from cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to remove product from cart: " + e.getMessage()));
        }
    }

    @PutMapping("/dealer/{dealerId}/product/{productId}")
    @Operation(summary = "Update Product Quantity",
               description = "Update quantity and price of a product in dealer's cart. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or price"),
            @ApiResponse(responseCode = "404", description = "Product not found in cart"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<CartResponse>> updateProductQuantity(
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable Long dealerId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "New quantity", required = true)
            @RequestParam Integer quantity,
            @Parameter(description = "Unit price", required = true)
            @RequestParam Double unitPrice) {

        log.info("Received update quantity request - dealer: {}, product: {}, quantity: {}, price: {}",
                dealerId, productId, quantity, unitPrice);

        try {
            CartResponse cartResponse = dealerCartService.updateProductQuantity(dealerId, productId, quantity, unitPrice);
            return ResponseEntity.ok(BaseResponse.success("Product quantity updated successfully", cartResponse));

        } catch (ResourceNotFoundException e) {
            log.error("Product not found in cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update product quantity: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to update product quantity: " + e.getMessage()));
        }
    }

}