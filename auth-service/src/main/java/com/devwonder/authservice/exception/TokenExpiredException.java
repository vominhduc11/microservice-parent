package com.devwonder.authservice.exception;

import com.devwonder.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseException {

    public TokenExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, cause);
    }
}