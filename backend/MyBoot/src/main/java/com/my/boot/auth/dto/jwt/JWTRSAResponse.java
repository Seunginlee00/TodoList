package com.my.boot.auth.dto.jwt;
public record JWTRSAResponse(
        String publicKey,
        String token,
        int expiresIn
) {
}


