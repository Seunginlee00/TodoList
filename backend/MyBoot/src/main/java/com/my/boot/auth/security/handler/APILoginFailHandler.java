package com.my.boot.auth.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class APILoginFailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.warn("❌ 로그인 실패: {}", exception.getMessage());

        String userId = (String) request.getAttribute("userId");

        Map<String, Object> errorMap = Map.of(
                "success", false,
                "error", "ERROR_LOGIN",
                "message", "로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.",
                "userId", userId != null ? userId : "unknown"
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), errorMap);
    }
}
