package com.my.boot.auth.security.util;

import com.my.boot.auth.service.EncryptService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTUtil {

    private static String SECRET_KEY = "5902371846593028475619283746501928374650";
    private final EncryptService encryptService; // 🔥 의존성 주입

    public String generateToken(Map<String, Object> valueMap, int min) {

        SecretKey key = null;

        try {
            key = Keys.hmacShaKeyFor(JWTUtil.SECRET_KEY.getBytes("UTF-8"));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        // 1. claims 복사 및 exp/iat 제거
        Map<String, Object> claims = new HashMap<>(valueMap);
        claims.remove("exp");
        claims.remove("iat");

        // 🔥 추가: userNm이 암호화되어 있다면 복호화 처리
        if (claims.containsKey("userNm")) {
            String userNm = (String) claims.get("userNm");
            if (userNm != null && isEncrypted(userNm)) {
                try {
                    String decryptedUserNm = encryptService.decryptUserNm(userNm);
                    claims.put("userNm", decryptedUserNm);
                    log.info("🔄 JWT 생성 시 userNm 복호화: {} -> {}", maskString(userNm), decryptedUserNm);
                } catch (Exception e) {
                    log.warn("⚠️ JWT 생성 시 userNm 복호화 실패: {}", maskString(userNm), e);
                    claims.put("userNm", "복호화실패");
                }
            }
        }

        // 2. 발급시간/만료시간 생성
        Date now = Date.from(ZonedDateTime.now().toInstant());
        Date exp = Date.from(ZonedDateTime.now().plusMinutes(min).toInstant());

        // 3. JWT 생성
        String token = Jwts.builder().header()
                .add("typ", "JWT")
                .add("alg", "HS256")
                .and()
                .issuedAt(now)
                .expiration(exp)
                .claims(claims)
                .signWith(key)
                .compact();

        // 🔥 추가: 생성된 토큰의 claims 로깅
        log.info("🔍 JWT 생성 완료 - claims: {}", claims);

        return token;

    }

    // 🔥 수정: static 제거 및 암호화 처리 추가
    public Map<String, Object> validateToken(String token) {
        SecretKey key = null;

        try {
            try {
                key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.info("🔍 토큰 검증 - claims: {}", claims);

            // 🔥 추가: 기존 토큰에 암호화된 userNm이 있다면 복호화 처리 (호환성)
            Map<String, Object> claimsMap = new HashMap<>(claims);
            if (claimsMap.containsKey("userNm")) {
                String userNm = (String) claimsMap.get("userNm");
                if (userNm != null && isEncrypted(userNm)) {
                    try {
                        String decryptedUserNm = encryptService.decryptUserNm(userNm);
                        claimsMap.put("userNm", decryptedUserNm);
                        log.info("🔄 JWT 검증 시 userNm 복호화: {} -> {}", maskString(userNm), decryptedUserNm);
                    } catch (Exception e) {
                        log.warn("⚠️ JWT 검증 시 userNm 복호화 실패: {}", maskString(userNm), e);
                        claimsMap.put("userNm", "복호화실패");
                    }
                }
            }

            return claimsMap;

        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token is expired", e);
            throw e;
        } catch (JwtException e) {
            log.error("❌ Invalid JWT detected", e);
            throw e;
        }
    }


    // refreshToken 유효성 체크 및 남은 시간 확인
    public long getExpireSeconds(String token) {
        SecretKey key = Keys.hmacShaKeyFor(JWTUtil.SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        return (expiration.getTime() - now) / 1000;
    }

    // 🔥 추가: 암호화된 문자열인지 확인하는 헬퍼 메서드
    private boolean isEncrypted(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // Base64 인코딩된 문자열 패턴 체크
        // 실제 암호화된 데이터는 Base64로 인코딩되어 있고 일정 길이 이상
        return value.matches("^[A-Za-z0-9+/]+=*$") && value.length() > 20;
    }

    // 🔥 추가: 로그에서 민감정보 마스킹
    private String maskString(String value) {
        if (value == null || value.length() <= 6) {
            return value;
        }
        return value.substring(0, 6) + "***";
    }

    // 🔥 추가: 기존 암호화된 토큰을 새로운 토큰으로 마이그레이션
    public String migrateEncryptedToken(String oldToken) {
        try {
            Map<String, Object> claims = validateToken(oldToken); // 이미 복호화 처리됨

            // 새로운 토큰 생성 (기본 10분)
            return generateToken(claims, 10);

        } catch (Exception e) {
            log.error("❌ 토큰 마이그레이션 실패", e);
            throw new RuntimeException("토큰 마이그레이션 실패", e);
        }
    }

}