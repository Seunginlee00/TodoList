package com.my.boot.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

//입력값을 정리·검증·포맷팅해주는 유틸리티 클래스

public class InputSanitizer {

    public static String XSSFilter(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        // XSS 필터링 (스크립트 태그 제거)
        String tmp = data.replaceAll("(?i)<(.*)s(.*)c(.*)r(.*)i(.*)p(.*)t", "");

        // 문자열이 아니면 null 반환 (Java에선 이 경우 거의 발생하지 않음)
        if (!(tmp instanceof String)) {
            return null;
        }

        // Injection 관련 문자열 escape 처리
        tmp = escapeString(tmp);

        // 위험 문자열 제거 (OS/SQL Injection 방지 목적)
        String[] dangerousChars = {"\\", "\"", "--", "(", ")", "#", ">", "<", "=", "*", "/", "+", "%", ";", ",", "|", "'"};
        for (String ch : dangerousChars) {
            tmp = tmp.replace(ch, "");
        }

        // & 추가 제거
        tmp = tmp.replace("&", "");

        return tmp.trim();
    }

    public static String formatPhoneNumber(String tel) {
        if (tel == null || tel.isBlank()) return "";

        // 숫자 이외 제거
        String onlyDigits = tel.replaceAll("[^0-9]", "");

        if (onlyDigits.startsWith("02")) {
            // 서울번호 (2자리 지역번호)
            return onlyDigits.replaceFirst("^(02)([0-9]{3,4})([0-9]{4})$", "$1-$2-$3");

        } else if (onlyDigits.startsWith("050")) {
            // 평생번호 (050x)
            return onlyDigits.replaceFirst("^(050[0-9])([0-9]{3})([0-9]{4})$", "$1-$2-$3");

        } else if (onlyDigits.length() == 8 &&
                (onlyDigits.startsWith("15") || onlyDigits.startsWith("16") || onlyDigits.startsWith("18"))) {
            // 지능망 번호
            return onlyDigits.replaceFirst("([0-9]{4})([0-9]{4})", "$1-$2");

        } else {
            // 일반 휴대폰/지역번호 (3자리 지역번호)
            return onlyDigits.replaceFirst("([0-9]{3})([0-9]{3,4})([0-9]{4})", "$1-$2-$3");
        }
    }

    public static String removeNonDigits(String tel) {
        if (tel == null) return "";
        return tel.replaceAll("[^0-9]", "");
    }

    private static String escapeString(String input) {
        // PHP addslashes()와 유사하게 \ ' " \0 를 이스케이프
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\'':
                case '"':
                case '\\':
                case '\0':
                    sb.append('\\');
                    // fallthrough
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 날짜 차이 (일 수 기준)
     * 예: "2024-04-01" ~ "2024-04-19" → 18
     */
    public static long getDateDiffInDays(String startDate, String endDate) {
        LocalDate d1 = LocalDate.parse(startDate); // ISO-8601 기본: yyyy-MM-dd
        LocalDate d2 = LocalDate.parse(endDate);
        return ChronoUnit.DAYS.between(d1, d2);
    }

    /**
     * 시간 차이 (초 기준)
     * 예: "2024-04-19 10:00:00" ~ "2024-04-19 10:05:00" → 300
     */
    public static long getTimeDiffInSeconds(String datetime1, String datetime2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt1 = LocalDateTime.parse(datetime1, formatter);
        LocalDateTime dt2 = LocalDateTime.parse(datetime2, formatter);
        return Duration.between(dt1, dt2).getSeconds();
    }

    /**
     * 날짜 차이 (년, 월, 일 기준) 반환
     * 예: {years=1, months=2, days=5}
     */
    public static Map<String, Long> getDetailedDateDiff(String startDate, String endDate) {
        LocalDate d1 = LocalDate.parse(startDate);
        LocalDate d2 = LocalDate.parse(endDate);
        long years = ChronoUnit.YEARS.between(d1, d2);
        long months = ChronoUnit.MONTHS.between(d1.plusYears(years), d2);
        long days = ChronoUnit.DAYS.between(d1.plusYears(years).plusMonths(months), d2);
        return Map.of("years", years, "months", months, "days", days);
    }

    public static String displayDate(String date) {
        if (date == null || date.isBlank()) return "";

        // 숫자만 추출
        String digits = date.replaceAll("[^0-9]", "");

        // yyyyMMdd 패턴에만 적용
        if (digits.matches("\\d{8}")) {
            return digits.replaceAll("(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3");
        }

        return digits; // fallback: 숫자만 반환
    }

    // 마지막 접속일자로부터 경과기간
    public static String lastLoginGap(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "미접속";
        }

        try {
            // 입력값에서 날짜만 추출
            LocalDate toDate = dateTime.toLocalDate();

            // 현재 날짜 (서울 기준)
            LocalDate fromDate = LocalDate.now(ZoneId.of("Asia/Seoul"));

            // 날짜 간 차이 계산
            Period period = Period.between(toDate, fromDate);
            long daysBetween = ChronoUnit.DAYS.between(toDate, fromDate);

            if (daysBetween < 7) {
                return "-";
            } else if (daysBetween < 30) {
                return daysBetween + "일";
            } else if (daysBetween > 365) {
                return period.getYears() + "년 " + period.getMonths() + "개월";
            } else {
                return period.getMonths() + "개월";
            }

        } catch (Exception e) {
            return "날짜 오류";
        }
    }

    public static String extractDateOnly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.toLocalDate().toString(); // "2025-05-17"
    }

    private static final String[] KOREAN_DAYS = {"일", "월", "화", "수", "목", "금", "토"};

    // LocalDate 버전
    public static String getWeekday(LocalDate date) {
        if (date == null) {
            return "날짜 없음";
        }
        int dayIndex = date.getDayOfWeek().getValue() % 7; // 일요일 = 0, 월요일 = 1 ...
        return KOREAN_DAYS[dayIndex];
    }

    // LocalDateTime 버전
    public static String getWeekday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "날짜 없음";
        }
        return getWeekday(dateTime.toLocalDate());
    }


}

