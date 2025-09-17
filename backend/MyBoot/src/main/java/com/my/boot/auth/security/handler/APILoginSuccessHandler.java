//package com.my.boot.auth.security.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import egovframe.mansa.smartx.api.logis.auth.dto.LoginResDTO;
//import egovframe.mansa.smartx.api.logis.auth.dto.MemberDTO;
//import egovframe.mansa.smartx.api.logis.auth.dto.passwd.PasswordExpireAlertDTO;
//import egovframe.mansa.smartx.api.logis.auth.security.util.JWTUtil;
//import egovframe.mansa.smartx.api.logis.auth.security.util.JwtTokenConstants;
//import egovframe.mansa.smartx.api.logis.auth.service.JwtTokenStoreService;
//import egovframe.mansa.smartx.api.logis.auth.service.LoginFailService;
//import egovframe.mansa.smartx.api.logis.auth.service.RSAService;
//import egovframe.mansa.smartx.api.logis.common.util.ClientInfo;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.Map;
//
//@Log4j2
//@RequiredArgsConstructor
//public class APILoginSuccessHandler implements AuthenticationSuccessHandler {
//    private final LoginFailService loginFailService;
//    private final JwtTokenStoreService jwtTokenStoreService;
//    private final JWTUtil jwtUtil;
//    private final RSAService rsaService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
//        log.info("authorities={}", authentication.getAuthorities());
//
//        log.info("✅ 로그인 성공 - userId: {}, userNm: {}, IP: {}",
//                memberDTO.getUserId(),
//                memberDTO.getUserNm(), // 이미 복호화된 상태여야 함
//                ClientInfo.getClientIP(request));
//
//        String userId = memberDTO.getUserId();
//        String clientIP = ClientInfo.getClientIP(request);
//
//        // 🔑 로그인 성공 시 JWT 키 삭제 (일회용 보장)
//        String jwtToken = (String) request.getAttribute("jwtToken");
//        if (jwtToken != null) {
//            rsaService.consumeJWTKey(jwtToken);
//        }
//
//        int resultCode = loginFailService.handleLoginSuccess(request, userId, clientIP); // 1:접속허용, 2:승인대기, 0:불허
//
//        if (resultCode == 11) {
//            // 🔒 비밀번호 유효성 판단
//            boolean isTempPassword = memberDTO.getPasswdChangeDate() == null;
//            boolean isPasswordExpired = memberDTO.getPasswdChangeDate() != null &&
//                    memberDTO.getPasswdChangeDate().isBefore(LocalDateTime.now().minusDays(90));
//
//            boolean mustChangePassword = isTempPassword || isPasswordExpired;
//
//            PasswordExpireAlertDTO passwordExpireAlertDTO = checkPasswordExpireAlert(memberDTO);
//
//            // 토큰 생성
//            Map<String, Object> claims = memberDTO.getClaims();
//            String accessToken = jwtUtil.generateToken(claims, JwtTokenConstants.ACCESS_TOKEN_MINUTES);
//            String refreshToken = jwtUtil.generateToken(claims, JwtTokenConstants.REFRESH_TOKEN_MINUTES);
//
//            // Redis에 AccessToken, RefreshToken 모두 저장
//            jwtTokenStoreService.saveTokenForUser(userId, accessToken);
//            jwtTokenStoreService.saveRefreshTokenForUser(userId, refreshToken);
//
//            // 📦 응답 DTO 조립
//            LoginResDTO loginResDTO = LoginResDTO.from(memberDTO, accessToken, refreshToken, resultCode,
//                    isTempPassword, isPasswordExpired, passwordExpireAlertDTO);
//
//            // 📤 JSON 응답 전송
//            response.setContentType("application/json; charset=UTF-8");
//            new ObjectMapper().writeValue(response.getWriter(), loginResDTO);
//
//            log.info("✅ 로그인 성공 - userId: {}, user:{}, IP: {}", userId, memberDTO, ClientInfo.getClientIP(request));
//        } else if(resultCode == 12) {
//            Map<String, Object> errorMap = Map.of(
//                    "error", "ERROR_LOGIN",
//                    "result", resultCode
//            );
//
//            response.setContentType("application/json; charset=UTF-8");
//            new ObjectMapper().writeValue(response.getWriter(), errorMap);
//        } else if(resultCode == 10) {
//            Map<String, Object> errorMap = Map.of(
//                    "error", "ERROR_LOGIN",
//                    "result", resultCode
//            );
//
//            response.setContentType("application/json; charset=UTF-8");
//            new ObjectMapper().writeValue(response.getWriter(), errorMap);
//        }
//
//    }
//
//    private PasswordExpireAlertDTO checkPasswordExpireAlert(MemberDTO memberDTO) {
//        LocalDateTime changeDate = memberDTO.getPasswdChangeDate();
//
//        if (changeDate == null) {
//            return PasswordExpireAlertDTO.of(false, -1);
//        }
//
//        long daysSinceChange = ChronoUnit.DAYS.between(changeDate, LocalDateTime.now());
//
//        int expireDays = 90;
//        int alertThreshold = 83;
//
//        if (daysSinceChange >= alertThreshold && daysSinceChange < expireDays) {
//            int daysUntilExpire = expireDays - (int) daysSinceChange;
//            return PasswordExpireAlertDTO.of(true, daysUntilExpire);
//        } else {
//            return PasswordExpireAlertDTO.of(false, -1);
//        }
//    }
//
//}
