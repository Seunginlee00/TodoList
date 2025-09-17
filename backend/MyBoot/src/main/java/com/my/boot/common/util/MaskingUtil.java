package com.my.boot.common.util;

public class MaskingUtil {

    // 휴대폰 번호 마스킹
    public static String phoneNoMasking(String str) {
        if (str == null || str.isEmpty()) return "";
        str = str.replaceAll("-", "");
        int length = str.length();

        switch (length) {
            case 10:
                return str.substring(0, 3) + "-" + str.substring(3, 4) + "**-*"
                        + str.substring(7, 10);
            case 11:
                return str.substring(0, 3) + "-" + str.substring(3, 5) + "**-*"
                        + str.substring(8, 11);
            default:
                return "";
        }
    }

    // ID 마스킹 (이메일 포함 여부 확인)
    public static String idMasking(String str) {
        if (str == null) return "";
        if (str.contains("@")) {
            return maskEmail(str);
        }
        return str.replaceAll(".{3}$", "***");
    }

    // 이메일 마스킹
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "";

        String[] parts = email.split("@");
        String id = parts[0];
        String domain = parts[1];

        String maskedId = id.length() <= 2
                ? id.charAt(0) + "*".repeat(id.length() - 1)
                : id.charAt(0) + "*".repeat(id.length() - 2) + id.charAt(id.length() - 1);

        String[] domainParts = domain.split("\\.");
        String domainMain = domainParts[0];
        String maskedDomain = domainMain.length() <= 2
                ? domainMain.charAt(0) + "*"
                : domainMain.charAt(0) + "*".repeat(domainMain.length() - 2) + domainMain.charAt(domainMain.length() - 1);

        String rest = domain.substring(domainMain.length());
        return maskedId + "@" + maskedDomain + rest;
    }

    // 대체용 이메일 마스킹 (정규식)
    public static String emailMasking(String str) {
        if (str == null) return "";
        return str.replaceAll("(\\w+)(\\w{3})(@.{1})([\\w*?]+)(.+)", "$1***$3*$5");
    }

    // IP 주소 마스킹
    public static String ipAddressMasking(String str) {
        if (str == null) return "";
        return str.replaceAll("(\\d+)([.]\\d+[.])(\\d+)([.]\\d+)", "***$2***$4");
    }

    // 기기 ID 전체 마스킹
    public static String deviceIDMasking(String str) {
        if (str == null) return "";
        return "*".repeat(str.length());
    }

    // 이름 마스킹 (마지막 글자만 *)
    public static String letterMasking(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.replaceAll(".(?!.)", "*");
    }
}

