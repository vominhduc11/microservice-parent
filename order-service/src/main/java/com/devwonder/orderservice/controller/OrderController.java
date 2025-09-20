package com.devwonder.orderservice.controller;

import com.devwonder.orderservice.dto.CreateOrderRequest;
import com.devwonder.orderservice.dto.OrderResponse;
import com.devwonder.orderservice.enums.PaymentStatus;
import com.devwonder.orderservice.service.OrderService;
import com.devwonder.common.dto.BaseResponse;
import com.devwonder.common.exception.ResourceNotFoundException;
import com.devwonder.common.validation.ValidId;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order/orders")
@Tag(name = "Order Management", description = "Order management endpoints for dealers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create Order",
               description = "Create a new order for a dealer. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> createOrder(
            @Parameter(description = "Order creation request", required = true)
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Received create order request for dealer: {}", request.getIdDealer());

        try {
            OrderResponse orderResponse = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success("Order created successfully", orderResponse));

        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get All Orders",
               description = "Retrieve all orders in the system. Requires ADMIN role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getAllOrders() {

        log.info("Received get all orders request");

        try {
            List<OrderResponse> orders = orderService.getAllOrders();
            return ResponseEntity.ok(BaseResponse.success("Orders retrieved successfully", orders));

        } catch (Exception e) {
            log.error("Failed to retrieve all orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Get Dealer Orders",
               description = "Retrieve all orders for a specific dealer. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getDealerOrders(
            @Parameter(description = "Dealer ID", required = true)
            @PathVariable @ValidId Long dealerId) {

        log.info("Received get orders request for dealer: {}", dealerId);

        try {
            List<OrderResponse> orders = orderService.getDealerOrders(dealerId);
            return ResponseEntity.ok(BaseResponse.success("Orders retrieved successfully", orders));

        } catch (Exception e) {
            log.error("Failed to retrieve dealer orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get Order by ID",
               description = "Retrieve a specific order by ID. Requires DEALER role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - DEALER role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable @ValidId Long orderId) {

        log.info("Received get order request for order: {}", orderId);

        try {
            OrderResponse orderResponse = orderService.getOrderById(orderId);
            return ResponseEntity.ok(BaseResponse.success("Order retrieved successfully", orderResponse));

        } catch (ResourceNotFoundException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to retrieve order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to retrieve order: " + e.getMessage()));
        }
    }

    @PatchMapping("/{orderId}/payment-status")
    @Operation(summary = "Update Payment Status",
               description = "Update payment status of an order. Requires ADMIN role authentication via API Gateway.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid payment status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<OrderResponse>> updatePaymentStatus(
            @Parameter(description = "Order ID", required = true)
            @PathVariable @ValidId Long orderId,
            @Parameter(description = "Payment status", required = true)
            @RequestParam PaymentStatus paymentStatus) {

        log.info("Received update payment status request for order: {} to status: {}", orderId, paymentStatus);

        try {
            OrderResponse orderResponse = orderService.updatePaymentStatus(orderId, paymentStatus);
            return ResponseEntity.ok(BaseResponse.success("Payment status updated successfully", orderResponse));

        } catch (ResourceNotFoundException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Failed to update payment status: " + e.getMessage()));
        }
    }
}