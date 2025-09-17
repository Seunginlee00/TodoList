package com.my.boot.config.filter;

import com.google.gson.Gson;
import com.my.boot.auth.security.socket.DuplicateLoginAlertSender;
import com.my.boot.auth.security.util.JWTUtil;
import com.my.boot.auth.service.JwtTokenStoreService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {
    private final JwtTokenStoreService jwtTokenStoreService;
    private final DuplicateLoginAlertSender duplicateLoginAlertSender;
    private final JWTUtil jwtUtil; // 🔥 JWTUtil 의존성 주입 추가

    // ✅ 제외할 URI를 Set 으로 미리 등록
    private static final Set<String> EXCLUDE_URIS = Set.of(
            "/check-access",
            "/api/pub-key",
            "/api/jwt-pub-key",
            "/api/public-key",
            "/api/member/login",
            "/api/member/register",
            "/api/member/refresh",
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh",
            // Swagger & OpenAPI - 반드시 아래 추가!
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs/main-api/**",
            "/swagger-resources/**",
            "/webjars/**"

    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("check uri.............." + path);

        // 🔥 Swagger/OpenAPI 리소스 예외처리를 먼저 처리 (가장 중요!)
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html")) {
            log.info("✅ Swagger 경로 허용: {}", path);
            return true;
        }

        // Preflight OPTIONS 요청은 무조건 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 로그인, 회원가입, public-key, refresh 만 예외로 허용
        if (EXCLUDE_URIS.contains(path)) {
            return true;
        }

        // 상품 이미지 조회 같은 공개용은 허용
        if (path.startsWith("/api/products/view/")) {
            return true;
        }

        // 사진 이미지 조회 같은 공개용은 허용
        if (path.startsWith("/api/member/photo/")) {
            return true;
        }


        // 나머지 요청은 전부 JWT 체크
        return false;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 중복 로그인 방지를 위해선 저장된 최신 JWT와 비교하는 로직을 doFilterInternal() 내에 추가해야 함

        log.info("------------------------JWTCheckFilter.......................");
        log.info("request uri: " + request);
        log.debug(">>> JWTCheckFilter invoked for URI: {}", request.getRequestURI());

        String authHeaderStr = request.getHeader("Authorization");
        // 1. Authorization 헤더 유효성 체크
        if (authHeaderStr == null || !authHeaderStr.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or malformed");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = authHeaderStr.substring(7);  // "Bearer " 제거

            Map<String, Object> claims = jwtUtil.validateToken(accessToken);

            String userId = (String) claims.get("userId");
            String userNm = (String) claims.get("userNm"); // 🔥 복호화된 사용자명

            // 🔥 추가: 토큰에서 추출한 정보 로깅 (복호화된 userNm 확인)
            log.info("🔍 JWT 토큰 검증 성공 - userId: {}, userNm: {}", userId, userNm);


            // Redis에 저장된 토큰과 일치 여부 확인
            String savedToken = jwtTokenStoreService.getTokenForUser(userId);
            if (savedToken == null || !savedToken.equals(accessToken)) {
                log.warn("❌ 중복 로그인 또는 로그아웃된 토큰 사용 - userId: {}", userId);
                // WebSocket 메시지 발송
                duplicateLoginAlertSender.sendDuplicateLoginAlert(userId);
                response.setContentType("application/json");
                String msg = new Gson().toJson(Map.of("error", "DUPLICATE_LOGIN"));
                response.getWriter().println(msg);
                SecurityContextHolder.clearContext();
                return;
            }

            // 권한 설정
            List<String> roleNames = (List<String>) claims.get("roles");
            if (roleNames == null) {
                roleNames = new ArrayList<>(); // 빈 리스트로 대체
            }

            // 권한 생성
            List<GrantedAuthority> authorities = roleNames.stream()
                    .filter(Objects::nonNull)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // 🔥 추가: 인증 객체에 추가 정보 설정 (userNm 등)
            authenticationToken.setDetails(Map.of(
                    "userId", userId,
                    "userNm", userNm,
                    "roles", roleNames
            ));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 🔥 추가: 요청에 사용자 정보 추가 (Controller에서 사용 가능)
            request.setAttribute("userId", userId);
            request.setAttribute("userNm", userNm);
            request.setAttribute("roles", roleNames);

            log.debug("✅ JWT 인증 성공 - userId: {}, authorities: {}", userId, authorities);

            filterChain.doFilter(request, response);

        } catch (Exception e) {

            log.error("❌ JWT Check Failed: {}", e.getMessage(), e);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String errorMsg = "인증 토큰이 유효하지 않습니다.";
            if (e.getMessage().contains("expired")) {
                errorMsg = "토큰이 만료되었습니다. 다시 로그인해주세요.";
            }

            String msg = new Gson().toJson(Map.of(
                    "error", "ERROR_ACCESS_TOKEN",
                    "message", errorMsg,
                    "timestamp", System.currentTimeMillis()
            ));

            try (PrintWriter writer = response.getWriter()) {
                writer.println(msg);
            }

            // 인증 정보 초기화
            SecurityContextHolder.clearContext();

        }
    }

    // 🔥 추가: 디버깅용 헬퍼 메서드
    private void logTokenInfo(Map<String, Object> claims) {
        if (log.isDebugEnabled()) {
            log.debug("🔍 토큰 Claims 정보:");
            claims.forEach((key, value) -> {
                if ("userNm".equals(key)) {
                    log.debug("  {}: {}", key, value); // 복호화된 userNm 확인
                } else {
                    log.debug("  {}: {}", key, value);
                }
            });
        }
    }

}
