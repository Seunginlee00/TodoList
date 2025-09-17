package com.my.boot.auth.service;

import com.my.boot.auth.security.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtTokenStoreService {
    private final StringRedisTemplate redisTemplate;
    private static final String ACCESS_PREFIX = "JWT:ACCESS:";
    private static final String REFRESH_PREFIX = "JWT:REFRESH:";

    private final JWTUtil jwtUtil;

    public void saveTokenForUser(String userId, String token) {
        Map<String, Object> claims = jwtUtil.validateToken(token);
        Long exp = (Long) claims.get("exp"); // JWT payload 에서 만료시간 추출
        long now = System.currentTimeMillis() / 1000; // 현재 시간 (초)

        long ttlSeconds = exp - now;
        if (ttlSeconds <= 0) {
            throw new RuntimeException("Token already expired.");
        }
        redisTemplate.opsForValue().set(ACCESS_PREFIX + userId, token, Duration.ofSeconds(ttlSeconds));
        //redisTemplate.opsForValue().set(PREFIX + userId, token, Duration.ofHours(2)); // 2시간 유효
    }

    // refreshToken 저장 추가
    public void saveRefreshTokenForUser(String userId, String refreshToken) {
        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);
        Long exp = (Long) claims.get("exp");
        long now = System.currentTimeMillis() / 1000;
        long ttlSeconds = exp - now;
        if (ttlSeconds <= 0) throw new RuntimeException("RefreshToken already expired.");
        redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    public String getRefreshTokenForUser(String userId) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX  + userId);
    }

    public String getTokenForUser(String userId) {
        return redisTemplate.opsForValue().get(ACCESS_PREFIX + userId);
    }

    public void removeTokenForUser(String userId) {
        redisTemplate.delete(ACCESS_PREFIX + userId);
    }

    public void removeRefreshTokenForUser(String userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }
}
