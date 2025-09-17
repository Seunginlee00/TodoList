package com.my.boot.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PasswdResDTO {
    private boolean success;
    private String message;
    private int resultCode;
    private String userId;

    public static PasswdResDTO success(String userId) {
        return PasswdResDTO.of(true, "SUCCESS", 0, userId);
    }

    public static PasswdResDTO fail(String message, int resultCode, String userId) {
        return PasswdResDTO.of(false, message, resultCode, userId);
    }
}
