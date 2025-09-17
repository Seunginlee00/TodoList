package com.my.boot.common.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ApiResult {

    private String status;
    private String message;
    private Object data;

    @Builder
    public ApiResult(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
