package com.my.boot.auth.dto.jwt;

public record JWTEncryptedRequest(
        String encryptedData,
        String token
) {
}
