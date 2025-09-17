package com.my.boot.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.boot.auth.dto.LoginReqDTO;
import com.my.boot.auth.service.RSAService;
import com.my.boot.common.util.ClientInfo;
import com.my.boot.common.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Log4j2
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final RSAService rsaService;
//    private final LoginFailService loginFailService;
//    private final LDAPAuthFailService ldapAuthFailService;
//    private final JwtTokenStoreService jwtTokenStoreService;

    public CustomLoginFilter(AuthenticationManager authenticationManager, RSAService rsaService


//                             LoginFailService loginFailService,
//                             LDAPAuthFailService ldapAuthFailService,
//                             JwtTokenStoreService jwtTokenStoreService
    ) {
        this.authenticationManager = authenticationManager;
        this.rsaService = rsaService;
//        this.loginFailService = loginFailService;
//        this.ldapAuthFailService = ldapAuthFailService;
//        this.jwtTokenStoreService = jwtTokenStoreService;

        // 로그인 성공/실패 핸들러
//        setAuthenticationSuccessHandler(new APILoginSuccessHandler(loginFailService, jwtTokenStoreService));
//        setAuthenticationFailureHandler(new APILoginFailHandler(loginFailService, ldapAuthFailService));

        setFilterProcessesUrl("/api/user/login"); // 로그인 처리 URL 지정
    }

    /**
     * LoginFilter는 기본적으로 x-www-form-urlencoded 데이터를 처리
     * UsernamePasswordAuthenticationFilter는 기본적으로 application/x-www-form-urlencoded 방식만 지원
     * JSON 요청(application/json)은 직접 request.getInputStream()으로 읽어야 함
     */

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String username = null;
        String password = null;
        String clientIP = ClientInfo.getClientIP(request);

        try {
            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                ObjectMapper mapper = new ObjectMapper();
                LoginReqDTO loginDTO = mapper.readValue(request.getInputStream(), LoginReqDTO.class);

                username = loginDTO.getUserId();
                password = loginDTO.getPassword();
            } else {
                username = obtainUsername(request);
                password = obtainPassword(request);
            }

            username = InputSanitizer.XSSFilter(username);

            log.info("🛡️ 로그인 시도 - IP: {}, userId: {}", clientIP, username);
            password = rsaService.decryptedText(password); // 🔓 필요한 경우 복호화 활성화

            request.setAttribute("userId", username); // 이게 있어야 위에서 읽힘

//            // ✅ 차단 로직
//            if (loginFailService.isIPBlocked(clientIP, 1) || loginFailService.isIPBlocked(clientIP, 2)) {
//                throw new BadCredentialsException("LOGIN_BLOCKED_IP");
//            }
//
//            // ✅ 계정 잠금 검사 (0 = 정상, 7 = 차단유지)
//            int lockResult = loginFailService.isUserLocked(request, username, clientIP);
//            if (lockResult == 7) {
//                throw new LoginFailedException("LOCKED_USER", username);
//            }
//
//            if (loginFailService.isUserExisted(username) == 0) {
//                log.info("🛡️ UNKNOWN_USER - IP: {}, userId: {}", clientIP, username);
//                throw new LoginFailedException("UNKNOWN_USER", username);
//            }

            // 통과 시 로그인 시도
            UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);

            setDetails(request, authRequest);

            return this.authenticationManager.authenticate(authRequest); // ✅ 핵심 수정
        } catch (IOException e) {
            throw new RuntimeException("Login request parsing error", e);
        }
    }
}
