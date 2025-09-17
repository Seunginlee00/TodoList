package com.my.boot.common.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ExceptionData error;

    public ApiException(ExceptionData error) {
        super(error.getMessage());
        this.error = error;
    }
}
