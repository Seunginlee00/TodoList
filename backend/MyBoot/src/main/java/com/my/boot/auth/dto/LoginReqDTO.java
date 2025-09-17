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
}
