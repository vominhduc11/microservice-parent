package com.devwonder.productservice.exception;

import com.devwonder.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientInventoryException extends BaseException {

    public InsufficientInventoryException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InsufficientInventoryException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, cause);
    }
}