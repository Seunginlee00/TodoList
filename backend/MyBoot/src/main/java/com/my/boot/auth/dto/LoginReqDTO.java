package com.my.boot.auth.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReqDTO {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;

    // JWT 토큰 (선택적 - JWT 방식 로그인 시에만 사용)
    private String token;
}
