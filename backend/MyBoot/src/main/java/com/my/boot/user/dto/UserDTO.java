package com.my.boot.user.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.my.boot.auth.service.EncryptService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
@Setter
@ToString
public class UserDTO extends User {

  private Long userId;
  private String loginId;
  private String passwd;
  private String salt;
  private String userNm; // 🔥 이 필드는 이미 복호화된 평문으로 저장됨
  private List<String> roleNames = new ArrayList<>();

  // 추가: 비밀번호 변경일자
  private LocalDateTime passwdChangeDate;
//  private String rnkNm;  // 계급
//  private String rspofcNm;  // 직책
//  private int orgId;

  // 🔥 수정: 생성자에서 암호화된 userNm을 받아 복호화 처리
  public UserDTO(String loginId, String passwd, String salt, String encryptedUserNm,
      List<String> roleNames, EncryptService encryptService) {
    super(
            loginId,
        passwd,
        roleNames.stream().map(str -> new SimpleGrantedAuthority("ROLE_"+str)).collect(
            Collectors.toList()));

    this.loginId = loginId;
    this.passwd = passwd;
    this.salt = salt;
    // 🔥 핵심: 암호화된 userNm을 복호화하여 저장
    this.userNm = decryptUserName(encryptedUserNm, encryptService);
    this.roleNames = roleNames;
  }

  // 🔥 수정: 오버로딩된 생성자도 복호화 처리
  public UserDTO(String userId, String passwd, String salt, String encryptedUserNm, List<String> roleNames,
      LocalDateTime passwdChangeDate,
//                 String rnkNm, String rspofcNm, int orgId,
      EncryptService encryptService
  ) {
    this(userId, passwd, salt, encryptedUserNm, roleNames, encryptService);
    this.passwdChangeDate = passwdChangeDate;
//    this.rnkNm = rnkNm;
//    this.rspofcNm = rspofcNm;
//    this.orgId = orgId;
  }

  // 🔥 추가: 안전한 복호화 처리 메서드
  private String decryptUserName(String encryptedUserNm, EncryptService encryptService) {
    if (encryptedUserNm == null || encryptedUserNm.isEmpty()) {
      return "이름없음";
    }

    try {
      return encryptService.decryptUserNm(encryptedUserNm);
    } catch (Exception e) {
      // 복호화 실패 시 기본값 반환 (로그는 서비스 레이어에서 처리)
      return "복호화실패";
    }
  }

  // 🔥 추가: 기존 생성자 호환성을 위한 Deprecated 생성자
  @Deprecated
  public UserDTO(String loginId, String passwd, String salt, String userNm, List<String> roleNames) {
    super(
        loginId,
        passwd,
        roleNames.stream().map(str -> new SimpleGrantedAuthority("ROLE_"+str)).collect(Collectors.toList()));

    this.loginId = loginId;
    this.passwd = passwd;
    this.salt = salt;
    this.userNm = userNm; // 이미 복호화된 것으로 가정
    this.roleNames = roleNames;
  }

  // 🔥 추가: 기존 호환성을 위한 Deprecated 생성자
  @Deprecated
  public UserDTO(String userId, String passwd, String salt, String userNm, List<String> roleNames,
      LocalDateTime passwdChangeDate, String rnkNm, String rspofcNm, int orgId
  ) {
    this(userId, passwd, salt, userNm, roleNames);
    this.passwdChangeDate = passwdChangeDate;
//    this.rnkNm = rnkNm;
//    this.rspofcNm = rspofcNm;
//    this.orgId = orgId;
  }

  public Map<String, Object> getClaims() {
    Map<String, Object> dataMap = new HashMap<>();

    dataMap.put("userId", userId);
    dataMap.put("userNm", userNm); // 이미 복호화된 평문
    dataMap.put("roles", roleNames);

    return dataMap;
  }

  // 🔥 추가: 정적 팩토리 메서드 (권장 방식)
  public static UserDTO createWithEncryption(String userId, String passwd, String salt,
      String encryptedUserNm, List<String> roleNames,
      EncryptService encryptService) {
    return new UserDTO(userId, passwd, salt, encryptedUserNm, roleNames, encryptService);
  }

  public static UserDTO createWithEncryption(String userId, String passwd, String salt,
      String encryptedUserNm, List<String> roleNames,
      LocalDateTime passwdChangeDate,
//        String rnkNm, String rspofcNm, int orgId,
      EncryptService encryptService) {
    return new UserDTO(userId, passwd, salt, encryptedUserNm, roleNames,
        passwdChangeDate,
//            rnkNm, rspofcNm, orgId,
            encryptService);
  }
}