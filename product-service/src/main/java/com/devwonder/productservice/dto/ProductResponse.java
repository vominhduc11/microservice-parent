package com.devwonder.productservice.dto;

import com.devwonder.productservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String image;
    private Object descriptions;
    private Object videos;
    private Object specifications;
    private BigDecimal price;
    private Object wholesalePrice;
    private Boolean showOnHomepage;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}