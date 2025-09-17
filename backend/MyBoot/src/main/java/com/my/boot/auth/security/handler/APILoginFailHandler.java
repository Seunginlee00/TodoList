//package com.my.boot.auth.security.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import egovframe.mansa.smartx.api.logis.auth.service.HrAuthFailService;
//import egovframe.mansa.smartx.api.logis.auth.service.LoginFailService;
//import egovframe.mansa.smartx.api.logis.common.util.ClientInfo;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Log4j2
//@RequiredArgsConstructor
//public class APILoginFailHandler implements AuthenticationFailureHandler {
//    private final LoginFailService loginFailService;
//    private final HrAuthFailService ldapAuthFailService;
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
//                                        AuthenticationException exception) throws IOException, ServletException {
//
//        log.warn("Login failed: {}", exception.getMessage());
//
//        String userId = (String) request.getAttribute("userId");
//        String clientIP = ClientInfo.getClientIP(request);
//
//        int resultCode = switch (exception.getMessage()) {
//            case "BLOCKED_IP" -> 8;
//            case "LOCKED_USER" -> 7;
//            case "UNKNOWN_USER" -> {
//                int failCount = ldapAuthFailService.getFailCount(userId);
//                log.info("🛡️ APILoginFailHandler UNKNOWN_USER: {}, failCount:{}", userId,failCount);
//                yield failCount >= 5 ? 5 : failCount; // 미가입자도 실패횟수 반영
//            }
//            default -> {
//                loginFailService.recordLoginFail(userId, request); // 일반 실패 시 기록
//                int failCount = loginFailService.getCurrentFailCount(userId);
//                yield failCount >= 5 ? 5 : failCount;
//            }
//        };
//
//        Map<String, Object> errorMap = Map.of(
//                "error", "ERROR_LOGIN",
//                "result", resultCode
//        );
//
//        response.setContentType("application/json; charset=UTF-8");
//        new ObjectMapper().writeValue(response.getWriter(), errorMap);
//    }
//
//}
