package com.devwonder.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long dealerId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Double totalPrice;
    private LocalDateTime lastUpdated;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemResponse {
        private Long productId;
        private Integer quantity;
        private Double unitPrice;
        private Double subtotal;
        private LocalDateTime addedAt;
    }
}