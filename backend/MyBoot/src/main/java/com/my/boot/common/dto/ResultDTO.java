package com.my.boot.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO<T> {

    private boolean success;     // 성공 여부
    private String message;      // 응답 메시지
    private T data;              // 실제 응답 데이터 (예: ProductDTO, ID 등)
}
