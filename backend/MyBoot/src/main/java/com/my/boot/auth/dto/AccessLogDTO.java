package com.my.boot.auth.dto;



public record AccessLogDTO(
      int no, // ✅ 일련번호 필드 추가
      String uid,  // 암호화된 UID
      String ipaddr,
      String date,
      String time,
      String OS,
      String browser,
      String login_id,
      String userNm,
      int success,
      int route,
      int errCode,
      String errorMessage // 추가: errCode → 메시지 매핑용
) {
}
