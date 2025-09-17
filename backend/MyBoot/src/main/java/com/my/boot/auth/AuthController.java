package com.my.boot.auth;

import com.my.boot.auth.dto.jwt.JWTEncryptedRequest;
import com.my.boot.auth.dto.jwt.JWTRSAResponse;
import com.my.boot.auth.service.RSAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
@Slf4j
@RestController  // 해당 Class 는 REST API 를 제공하는 Controller
@RequestMapping("/api")  // RequestMapping URI 를 지정해주는 Annotation
@RequiredArgsConstructor
public class AuthController {
    private final RSAService rsaService;


    /**
     * 비번 변경 공개키
     */
    @GetMapping("/pub-key")  // @GetMapping : Http GetMethod URL 주소를 매핑
    public Map<String, String> getPubKey() {
        return Map.of("publicKey", rsaService.getPubKey());
    }

    /**
     * JWT 방식 로그인용 공개키 (기존 /api/public-key 대체)
     * Front-End에서 경로만 변경하면 바로 사용 가능
     */
    @GetMapping("/jwt-pub-key")
    public Map<String, Object> getJwtPublicKey() {
        // 임시 사용자 ID (실제로는 세션이나 다른 방식으로 식별)
        String userId = "temp_" + UUID.randomUUID().toString();

        JWTRSAResponse response = rsaService.generateJWTRSAKey(userId);

        log.info("JWT-RSA 키 발급 완료 - 임시사용자: {}", userId);

        // 기존 AuthController와 동일한 형태로 응답
        return Map.of(
                "publicKey", response.publicKey(),
                "token", response.token(),
                "expiresIn", response.expiresIn()
        );
    }

    /**
     * JWT 토큰으로 암호화된 데이터 복호화
     * 로그인 처리 시 호출
     */
    @PostMapping("/decrypt-jwt")
    public ResponseEntity<Map<String, String>> decryptJwtData(@RequestBody JWTEncryptedRequest request) {
        try {
            String decryptedData = rsaService.decryptWithJWT(request);

            // 복호화 성공 응답
            Map<String, String> response = Map.of(
                    "status", "success",
                    "decryptedData", decryptedData
            );

            log.info("JWT-RSA 복호화 성공");
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("JWT-RSA 복호화 보안 에러: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("JWT-RSA 복호화 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "복호화 처리 중 오류가 발생했습니다."));
        }
    }

}
