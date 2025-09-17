package com.my.boot.auth.service;

import com.my.boot.auth.dto.jwt.JWTEncryptedRequest;
import com.my.boot.auth.dto.jwt.JWTRSAResponse;
import com.my.boot.auth.security.util.JWTUtil;
import com.my.boot.common.util.CryptoUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RSAService {

    private final CryptoUtil cryptoUtil;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 비밀번호 변경용 고정 키 (최소한 유지)
    private String base64PublicKey;
    private String base64PrivateKey;

    @PostConstruct
    private void initializeFixedRSAKey() {
        generateFixedRSAKey(); // 비밀번호 변경용 고정 키 생성
    }

    /**
     * 비밀번호 변경용 고정 RSA 키 생성
     */
    private void generateFixedRSAKey() {
        KeyPair keyPair = cryptoUtil.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        base64PrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        log.info("🔑 비밀번호 변경용 고정 RSA 키 생성 완료");
    }

    public String getPubKey() {
        return base64PublicKey;
    }

    public String decryptedText(String encryptedMessage) {
        return cryptoUtil.decryptRSA(encryptedMessage, base64PrivateKey);
    }

    // ==================== JWT 방식 메서드들 ====================

    public JWTRSAResponse generateJWTRSAKey(String userId) {
        KeyPair keyPair = cryptoUtil.generateRSAKeyPair();

        String keyId = UUID.randomUUID().toString();
        Map<String, Object> claims = Map.of(
                "keyId", keyId,
                "userId", userId
        );

        String jwt = jwtUtil.generateToken(claims, 5); // 5분 후 만료

        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        redisTemplate.opsForValue().set("rsa_key:" + keyId, privateKey, Duration.ofMinutes(5));

        log.info("🔑 JWT-RSA 키 발급 완료 - userId: {}, keyId: {}", userId, keyId);

        return new JWTRSAResponse(
                publicKey,
                jwt,
                300
        );
    }

    /**
     * JWT 토큰 검증 후 복호화 (키는 삭제하지 않음)
     */
    public String decryptWithJWT(JWTEncryptedRequest request) {
        try {
            Map<String, Object> claims = jwtUtil.validateToken(request.token());
            String keyId = (String) claims.get("keyId");
            String userId = (String) claims.get("userId");

            if (keyId == null || userId == null) {
                throw new SecurityException("유효하지 않은 토큰입니다.");
            }

            // 키 조회만 하고 삭제하지 않음 (재시도 가능하도록)
            String privateKey = redisTemplate.opsForValue().get("rsa_key:" + keyId);
            if (privateKey == null) {
                throw new SecurityException("키가 만료되었거나 존재하지 않습니다.");
            }

            String decrypted = cryptoUtil.decryptRSA(request.encryptedData(), privateKey);

            log.info("🔓 JWT-RSA 복호화 성공 - userId: {}, keyId: {}", userId, keyId);
            return decrypted;

        } catch (Exception e) {
            log.error("❌ JWT-RSA 복호화 실패: {}", e.getMessage());
            throw new SecurityException("JWT-RSA 복호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 로그인 성공 시 JWT 키 삭제 (일회용 보장)
     */
    public void consumeJWTKey(String jwtToken) {
        try {
            Map<String, Object> claims = jwtUtil.validateToken(jwtToken);
            String keyId = (String) claims.get("keyId");

            if (keyId != null) {
                redisTemplate.delete("rsa_key:" + keyId);
                log.info("🗑️ JWT-RSA 키 삭제 완료 - keyId: {}", keyId);
            }
        } catch (Exception e) {
            log.warn("JWT 키 삭제 중 오류: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredJWTKeys() {
        try {
            Set<String> keys = redisTemplate.keys("rsa_key:*");
            if (keys != null && !keys.isEmpty()) {
                int deletedCount = 0;
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(key);
                        deletedCount++;
                    }
                }
                if (deletedCount > 0) {
                    log.debug("🧹 만료된 JWT-RSA 키 정리 완료: {}개", deletedCount);
                }
            }
        } catch (Exception e) {
            log.error("JWT-RSA 키 정리 중 오류 발생", e);
        }
    }

    public Map<String, Object> getSystemStatus() {
        try {
            Set<String> jwtKeys = redisTemplate.keys("rsa_key:*");
            int jwtKeyCount = (jwtKeys != null) ? jwtKeys.size() : 0;

            return Map.of(
                    "fixedKeyGenerated", base64PublicKey != null,
                    "jwtKeyCount", jwtKeyCount,
                    "systemTime", System.currentTimeMillis()
            );
        } catch (Exception e) {
            log.error("시스템 상태 조회 중 오류", e);
            return Map.of("error", e.getMessage());
        }
    }
}
