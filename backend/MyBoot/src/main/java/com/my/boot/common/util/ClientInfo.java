package com.my.boot.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientInfo {

  // ip 정보 반환
  public static String getClientIP(HttpServletRequest request) {
    String[] headerNames = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    for (String header : headerNames) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // ✅ 다중 IP인 경우 첫 번째만 반환
        return ip.split(",")[0].trim();
      }
    }

    String ip = request.getRemoteAddr();

    // ✅ IPv6 loopback → IPv4로 치환
    if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
      return "127.0.0.1";
    }

    return ip;
  }

  // os 정보 반환
  public static String getClientOS(HttpServletRequest request) {
    String deviceType = request.getHeader("X-Device-Type");
    if (deviceType != null) {
      if (deviceType.equalsIgnoreCase("android")) return "Android-App";
      else if (deviceType.equalsIgnoreCase("ios")) return "iOS-App";
    }

    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null) return "Unknown";

    userAgent = userAgent.toLowerCase();
    if (userAgent.contains("android-app")) return "Android-App";
    if (userAgent.contains("iphone") || userAgent.contains("ipad")) return "iOS";
    else if (userAgent.contains("android")) return "Android";
    else if (userAgent.contains("windows")) return "Windows";
    else if (userAgent.contains("mac")) return "Mac";
    else if (userAgent.contains("x11")) return "Unix";
    else return "Unknown";
  }

  // 브라우저 정보 반환
  public static String getBrowser(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null) return "Unknown Browser";

    userAgent = userAgent.toLowerCase();

    // 커스텀 User-Agent 대응
    if (userAgent.contains("android-app")) return "Android-App";
    if (userAgent.contains("ios-app")) return "iOS-App";

    String browser = "Unknown Browser";
    Map<String, String> browserPatterns = new LinkedHashMap<>();
    browserPatterns.put("msie",        "Internet Explorer");
    browserPatterns.put("trident",     "Internet Explorer"); // IE 11+
    browserPatterns.put("edge",        "Edge");
    browserPatterns.put("firefox",     "Firefox");
    browserPatterns.put("chrome",      "Chrome");
    browserPatterns.put("safari",      "Safari");
    browserPatterns.put("opera",       "Opera");
    browserPatterns.put("opr",         "Opera");
    browserPatterns.put("netscape",    "Netscape");
    browserPatterns.put("maxthon",     "Maxthon");
    browserPatterns.put("konqueror",   "Konqueror");
    browserPatterns.put("mobile",      "Mobile Browser");

    userAgent = userAgent.toLowerCase();

    for (Map.Entry<String, String> entry : browserPatterns.entrySet()) {
      if (userAgent.contains(entry.getKey())) {
        browser = entry.getValue();
        break;
      }
    }

    return browser;
  }

  /**
   * 디바이스 타입 추론
   * 1 = iPhone/iPad (iOS)
   * 2 = Android
   * 3 = Windows PC/Laptop
   * 4 = Mac (macOS PC/Laptop)
   * 기본값은 4 (Mac)으로 처리
   */
  public static int getDeviceType(HttpServletRequest request) {
    // 1. 커스텀 헤더 우선 처리
    String deviceHeader = request.getHeader("X-Device-Type");
    if (deviceHeader != null) {
      switch (deviceHeader.toLowerCase()) {
        case "ios": return 1;
        case "android": return 2;
        case "mac": return 4;
        case "windows":
        case "pc":
        case "desktop": return 3;
      }
    }

    // 2. User-Agent 기반 추론
    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null || userAgent.isBlank()) return 4;

    String ua = userAgent.toLowerCase();
    if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) return 1;
    if (ua.contains("android-app") || ua.contains("android")) return 2;
    if (ua.contains("windows")) return 3;
    if (ua.contains("macintosh") || ua.contains("mac os")) return 4;

    return 4; // 기본값: 모르는 것은 4로 처리
  }

}

