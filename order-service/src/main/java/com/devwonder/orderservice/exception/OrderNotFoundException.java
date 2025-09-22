package com.devwonder.orderservice.exception;

import com.devwonder.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BaseException {

    public OrderNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}