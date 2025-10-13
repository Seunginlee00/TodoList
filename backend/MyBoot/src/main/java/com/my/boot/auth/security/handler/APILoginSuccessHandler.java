package com.my.boot.auth.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.boot.auth.security.util.JWTUtil;
import com.my.boot.auth.service.JwtTokenStoreService;
import com.my.boot.user.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenStoreService jwtTokenStoreService;
    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserDTO userDTO = (UserDTO) authentication.getPrincipal();

        log.info("✅ 로그인 성공 - userId: {}, userNm: {}",
                userDTO.getLoginId(),
                userDTO.getUserNm());

        String userId = userDTO.getLoginId();

        // JWT 토큰 생성
        Map<String, Object> claims = userDTO.getClaims();
        String accessToken = jwtUtil.generateToken(claims, 10); // 10분
        String refreshToken = jwtUtil.generateToken(claims, 60 * 24); // 1일

        // Redis에 토큰 저장 (중복 로그인 방지)
        jwtTokenStoreService.saveTokenForUser(userId, accessToken);
        jwtTokenStoreService.saveRefreshTokenForUser(userId, refreshToken);

        // 📦 응답 데이터 생성
        Map<String, Object> responseData = Map.of(
                "success", true,
                "userId", userId,
                "userNm", userDTO.getUserNm(),
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "roles", userDTO.getRoleNames()
        );

        // 📤 JSON 응답 전송
        response.setContentType("application/json; charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), responseData);

        log.info("✅ JWT 토큰 발급 완료 - userId: {}", userId);
    }
}
