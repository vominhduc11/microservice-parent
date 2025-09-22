package com.devwonder.authservice.exception;

import com.devwonder.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AccountAlreadyExistsException extends BaseException {

    public AccountAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public AccountAlreadyExistsException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, cause);
    }
}