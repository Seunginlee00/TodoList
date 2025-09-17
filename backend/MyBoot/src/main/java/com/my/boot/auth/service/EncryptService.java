package com.my.boot.auth.service;


import java.util.Base64;

import com.my.boot.common.config.SafeDBKeyConfig;
import com.my.boot.common.util.MockSafeDB;
import org.springframework.stereotype.Service;

@Service
public class EncryptService {
  private static final MockSafeDB safeDB = MockSafeDB.getInstance(SafeDBKeyConfig.hexKey);

  public String encrypt(String userName, String tableName, String columnName, String plain) {
    try {
      byte[] enc = safeDB.encrypt(userName, tableName, columnName, plain.getBytes());
      return Base64.getEncoder().encodeToString(enc);
    } catch (Exception e) {
      throw new RuntimeException("암호화 오류", e);
    }
  }

  public String decrypt(String userName, String tableName, String columnName, String encrypted) {
    try {
      byte[] enc = Base64.getDecoder().decode(encrypted);
      byte[] dec = safeDB.decrypt(userName, tableName, columnName, enc);
      return new String(dec);
    } catch (Exception e) {
      throw new RuntimeException("복호화 오류", e);
    }
  }

  // === 전용 헬퍼 함수 예시 ===

  /** 성명 암호화 */
  public String encryptUserNm(String plain) {
    return encrypt(
        SafeDBKeyConfig.userName,
        SafeDBKeyConfig.tableName,
        SafeDBKeyConfig.columnName_userNm,
        plain
    );
  }

  /** 성명 복호화 */
  public String decryptUserNm(String encrypted) {
    return decrypt(
        SafeDBKeyConfig.userName,
        SafeDBKeyConfig.tableName,
        SafeDBKeyConfig.columnName_userNm,
        encrypted
    );
  }

  /** 휴대폰번호 암호화 */
  public String encryptMobileNo(String plain) {
    return encrypt(
        SafeDBKeyConfig.userName,
        SafeDBKeyConfig.tableName,
        SafeDBKeyConfig.columnName_mobileNo,
        plain
    );
  }

  /** 휴대폰번호 복호화 */
  public String decryptMobileNo(String encrypted) {
    return decrypt(
        SafeDBKeyConfig.userName,
        SafeDBKeyConfig.tableName,
        SafeDBKeyConfig.columnName_mobileNo,
        encrypted
    );
  }
}
