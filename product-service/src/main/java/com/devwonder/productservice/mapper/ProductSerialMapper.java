package com.devwonder.productservice.mapper;

import com.devwonder.productservice.dto.ProductSerialResponse;
import com.devwonder.productservice.entity.ProductSerial;
import org.springframework.stereotype.Component;

@Component
public class ProductSerialMapper {
    
    public ProductSerialResponse toProductSerialResponse(ProductSerial productSerial) {
        return ProductSerialResponse.builder()
                .id(productSerial.getId())
                .serial(productSerial.getSerial())
                .productId(productSerial.getProduct().getId())
                .productName(productSerial.getProduct().getName())
                .status(productSerial.getStatus())
                .build();
    }
}