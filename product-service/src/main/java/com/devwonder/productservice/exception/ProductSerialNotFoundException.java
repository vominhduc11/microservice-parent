package com.devwonder.productservice.exception;

import com.devwonder.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ProductSerialNotFoundException extends BaseException {

    public ProductSerialNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ProductSerialNotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}