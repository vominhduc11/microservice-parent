package com.devwonder.productservice.dto;

import com.devwonder.productservice.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    
    private String sku;
    
    private String name;
    
    private String images; // Base64 image data

    private Object description;

    private Object videos;

    private Object specifications;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    private Object wholesalePrice;
    
    
    private Boolean showOnHomepage;
    
    private Boolean isFeatured;
}