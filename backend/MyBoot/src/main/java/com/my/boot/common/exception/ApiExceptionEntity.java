package com.my.boot.common.exception;


public record ApiExceptionEntity(
    String errorCode,
    String errorMessage
) {

    public ApiExceptionEntity(ApiException e) {
        this(
            e.getError().getCode(),
            e.getError().getMessage()
        );
    }

    public ApiExceptionEntity(RuntimeException e) {
        this(
            ExceptionData.RUNTIME_EXCEPTION.getCode(),
            e.getMessage()
        );
    }

    public ApiExceptionEntity(Exception e) {
        this(
            ExceptionData.INTERNAL_SERVER_EXCEPTION.getCode(),
            e.getMessage()
        );
    }
}