package com.devwonder.orderservice.exception;

import com.devwonder.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidOrderStatusException extends BaseException {

    public InvalidOrderStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, cause);
    }
}