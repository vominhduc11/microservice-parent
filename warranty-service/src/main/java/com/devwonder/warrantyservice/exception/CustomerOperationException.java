package com.devwonder.warrantyservice.exception;

import com.devwonder.common.exception.BaseException;

public class CustomerOperationException extends BaseException {

    public CustomerOperationException(String message) {
        super(message);
    }

    public CustomerOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "CUSTOMER_OPERATION_FAILED";
    }
}