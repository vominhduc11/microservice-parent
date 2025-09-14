package com.devwonder.productservice.dto;

import com.devwonder.productservice.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String images; // Base64 image data

    private Object description;

    private Object videos;

    private Object specifications;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price; // Changed from retailPrice to price

    @NotNull(message = "Wholesale price is required")
    private Object wholesalePrice;
    
    
    private Boolean showOnHomepage = false;
    
    private Boolean isFeatured = false;
}