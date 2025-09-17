package com.my.boot.common.util;

import com.my.boot.auth.dto.PasswdResDTO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

public class PasswordUtil {
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile("(\\w)\\1{3}");
    private static final Pattern SIMPLE_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).{10,15}$");
    private static final Pattern COMPLEX_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,15}$");

    public static PasswdResDTO validate(String curPW, String newPW, String userId) {

        if (curPW == null || curPW.isBlank()) {
            return PasswdResDTO.fail("현재 비밀번호를 입력하세요", 101, userId);
        }

        if (newPW == null || newPW.isBlank()) {
            return PasswdResDTO.fail("변경할 비밀번호를 입력하세요", 102, userId);
        }

        if (newPW.contains(" ")) {
            return PasswdResDTO.fail("공백은 입력할 수 없습니다.", 103, userId);
        }

        if (curPW.equals(newPW)) {
            return PasswdResDTO.fail("현재 비밀번호를 그대로 사용할 수 없습니다.", 104, userId);
        }

        if (!(SIMPLE_PATTERN.matcher(newPW).matches() || COMPLEX_PATTERN.matcher(newPW).matches())) {
            return PasswdResDTO.fail("영문/숫자 혼용 시에는 10~15자리를,\n영문/숫자/특수문자 혼용 시에는 8~15자리를 사용해야 합니다.", 105, userId);
        }

        if (REPEATED_CHAR_PATTERN.matcher(newPW).find()) {
            return PasswdResDTO.fail("같은 문자를 4번 이상 사용할 수 없습니다.", 106, userId);
        }

        if (isSequential(newPW)) {
            return PasswdResDTO.fail("비밀번호에 4자 이상의 연속 문자 또는 숫자를 사용할 수 없습니다.", 107, userId);
        }

        return PasswdResDTO.success(userId);
    }

    private static boolean isSequential(String value) {
        for (int i = 0; i < value.length() - 3; i++) {
            int t0 = value.charAt(i);
            int t1 = value.charAt(i + 1);
            int t2 = value.charAt(i + 2);
            int t3 = value.charAt(i + 3);

            if ((t0 - t1 == 1 && t1 - t2 == 1 && t2 - t3 == 1) ||
                    (t0 - t1 == -1 && t1 - t2 == -1 && t2 - t3 == -1)) {
                return true;
            }
        }
        return false;
    }


    // PHP의 hashSSHA()에 대응 (SALT = 20자리 문자열)
    public static String getSalt() {
        byte[] saltBytes = new byte[20]; // 20바이트 = PHP substr(sha1, 0, 20)
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes).substring(0, 20);
    }

    // PHP hashSSHA와 완전 동일한 방식
    public static String hashSSHA(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));

            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

            // 해시 + salt 바이트 결합
            byte[] combined = new byte[hashBytes.length + saltBytes.length];
            System.arraycopy(hashBytes, 0, combined, 0, hashBytes.length);
            System.arraycopy(saltBytes, 0, combined, hashBytes.length, saltBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing error", e);
        }
    }

    // PHP의 checkhashSSHA와 동일
    public static boolean matches(String plainPassword, String encrypted, String salt) {
        return hashSSHA(plainPassword, salt).equals(encrypted);
    }
}

