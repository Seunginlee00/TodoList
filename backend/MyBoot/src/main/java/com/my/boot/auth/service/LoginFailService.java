//package com.my.boot.auth.service;
//
//
//import com.my.boot.common.util.ClientInfo;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//
//@Service
//@RequiredArgsConstructor
//@Log4j2
//public class LoginFailService {
//    private final AccessLogService accessLogService;
//    private final HrAuthFailService ldapAuthFailService;
//    private final MemberMapper memberMapper;
//    private final BlockLogMapper blockLogMapper;
//    private final AllowHostMapper allowHostMapper;
//
//    /**
//     * IP 차단 여부 확인 후 해제 가능 여부 판단
//     */
//    public boolean isIPBlocked(String ip, int type) {
//        int checkResult = accessLogService.checkIPBlock(ip, type);
//        return checkResult == 8; // 8: 차단 유지 중
//    }
//
//    /**
//     * 로그인 실패 5회 이상이면 시간 차 검사 후 계정 잠금 판단
//     */
//    public int isUserLocked(HttpServletRequest request, String userId, String clientIP) {
//        // 사용자 정보가 존재하는지 확인
//        boolean userExists = memberMapper.countByUserId(userId) > 0;
//        int route = getDeviceType(request);
//
//        if (userExists) {
//            Integer failCount = memberMapper.findAccessFailedCountByUserId(userId);
//            if (failCount == null) failCount = 0;
//            if (failCount >= 5) {
//                int gapTime = checkUserAccessDateDiff(userId); // 5분 경과 여부
//                if (gapTime == 1) {
//                    log.info("🔓 [계정차단 해제] userId={}, last accessDate 경과됨. 계정잠금 해제", userId);
//                    memberMapper.clearAccountLock(userId); // access=1, 실패횟수 초기화
//                    return 0;
//                } else {
//                    log.info("⛔ [계정차단 유지] userId={}, 아직 5분 경과 안됨", userId);
//                    accessLogService.saveAccessLog(request, userId, 0, route, 2, null);
//                    return 7;
//                }
//            }
//        } else {
//            int failCount = ldapAuthFailService.getFailCount(userId);
//            log.info("⛔ [사용자 없음] userId={} 미가입자, failCount={} ", userId, failCount);
//            if (failCount >= 5) {
//                int gapTime = ldapAuthFailService.checkTimeDiff(clientIP); // 5분 기준
//                if (gapTime == 1) {
//                    log.info("🔓 [계정차단 해제] userId={} 미가입자, last accessDate 경과됨. 계정잠금 해제", userId);
//                    ldapAuthFailService.clearIPAddressLock(clientIP);
//                    return 0;
//                } else {
//                    log.info("⛔ [계정차단 유지] userId={} 미가입자, 아직 5분 경과 안됨", userId);
//                    accessLogService.saveAccessLog(request, userId, 0, route, 5, null);
//                    ldapAuthFailService.incrementFail(userId, 0, clientIP);
//                    return 7;
//                }
//            } else {
//                if (!ldapAuthFailService.isFailRecordExists(userId, clientIP)) {
//                    accessLogService.saveAccessLog(request, userId, 0, route, 5, null);
//                    ldapAuthFailService.createFailUser(userId, 0, clientIP);
//                } else {
//                    accessLogService.saveAccessLog(request, userId, 0, route, 5, null);
//                    ldapAuthFailService.incrementFail(userId, 0, clientIP);
//                }
//                return ldapAuthFailService.getFailCount(userId);
//            }
//        }
//        return 0; // 차단 조건 아님
//    }
//
//    public int checkUserAccessDateDiff(String userId) {
//        final int LIMIT_SECONDS = 300;
//        LocalDateTime accessDate = memberMapper.findAccessDateByUserId(userId);
//        if (accessDate != null) {
//            //log.debug("🕓 [DB AccessDate 조회] userId={}, accessDate={}", userId, accessDate); // ✅ 먼저 로그 출력
//            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//            long seconds = Duration.between(accessDate, now).getSeconds();
//            return seconds > LIMIT_SECONDS ? 1 : 0;
//        } else {
//            log.debug("⚠️ [TimeGap Check] userId={}, accessDate 없음", userId); // 조회 결과 없음도 로그
//        }
//        return 0; // 조회 결과가 없으면 기본값 0
//    }
//
//    /**
//     * 로그인 실패 기록 및 실패 횟수 증가
//     */
//    public void recordLoginFail(String userId, HttpServletRequest request) {
//        accessLogService.saveAccessLog(request, userId, 0, 1, 5, null);
//        memberMapper.incrementLoginFail(LocalDateTime.now(), userId);
//    }
//
//    /**
//     * 실패 횟수 만료 시 초기화
//     */
//    public void clearLoginFail(String userId) {
//        memberMapper.clearLoginFailCount(userId);
//    }
//
//    /**
//     * 현재 실패 횟수 반환
//     */
//    public int getCurrentFailCount(String userId) {
//        Integer failCount = memberMapper.findAccessFailedCountByUserId(userId);
//        return failCount != null ? failCount : 0;
//    }
//
//    public int isUserExisted(String userId) {
//        return memberMapper.countByUserId(userId);
//    }
//
//    private void clearUserIDLock(String userId) {
//        memberMapper.clearLoginFailCount(userId);
//    }
//
//    private int isUserStatusChk(String userId) {
//        Integer status = memberMapper.findAccessStatusByUserId(userId);
//        return status != null ? status : 0;
//    }
//
//    public int handleLoginSuccess(HttpServletRequest request, String userId, String clientIP) {
//        int status = isUserStatusChk(userId); // 1:접속허용, 2:승인대기, 0:불허
//        int route = getDeviceType(request);
//
//        if (status == 1) {
//            // 로그인 성공 시 로그인 실패 횟수 초기화
//            clearUserIDLock(userId);
//            ldapAuthFailService.clearIPAddressLock(clientIP);
//            accessLogService.saveAccessLog(request, userId, 1, route, 0, null);
//            // 마지막 로그인 시간 업데이트
//            memberMapper.updateLastLoginDate(userId, LocalDateTime.now());
//
//            return 11; // 접속 허용
//        } else if (status == 2) {
//            accessLogService.saveAccessLog(request, userId, 0, route, 3, null); // 승인대기
//            return 12; // 승인 대기
//        } else if (status == 0) {
//            accessLogService.saveAccessLog(request, userId, 0, route, 1, null); // 관리자 불허
//            return 10; // 접속 불허
//        }
//
//        return 0; // 기타 예외 상황
//    }
//
//    public boolean allowIPChk(int mtype, HttpServletRequest request) {
//        if (mtype == 1 || mtype == 2) return true;
//
//        String clientIP = ClientInfo.getClientIP(request);
//        long count = allowHostMapper.countByIpaddrAndPermission(clientIP, 0);
//        return count == 1;
//    }
//
//}