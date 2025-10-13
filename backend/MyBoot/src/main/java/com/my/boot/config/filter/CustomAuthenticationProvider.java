package com.my.boot.config.filter;

import com.my.boot.auth.service.CustomUserDetailsService;
import com.my.boot.common.util.PasswordUtil;
import com.my.boot.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

  private final CustomUserDetailsService userDetailsService;
  private final CustomPasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String userId = authentication.getName();
    String rawPassword = authentication.getCredentials().toString();

    log.info("🔐 로그인 검증 시작 - userId: {}", userId);

    UserDTO userDetails = (UserDTO) userDetailsService.loadUserByUsername(userId);
    String encoded = userDetails.getPassword();
    String salt = userDetails.getSalt();

    log.info("✅ DB에서 사용자 정보 로드 완료");
    log.info("📦 저장된 해시: {}", encoded);
    log.info("🧂 저장된 salt: {}", salt);

    // 로그인 시 입력한 비밀번호로 해시 생성
    String loginHash = PasswordUtil.hashSSHA(rawPassword, salt);
    log.info("🔑 로그인 시도 해시: {}", loginHash);

    if (!passwordEncoder.matches(rawPassword, salt, encoded)) {
      log.error("❌ 비밀번호 불일치 - userId: {}", userId);
      log.error("   입력 해시: {}", loginHash);
      log.error("   저장 해시: {}", encoded);
      throw new BadCredentialsException("비밀번호 불일치");
    }

    log.info("✅ 비밀번호 검증 성공 - userId: {}", userId);
    return new UsernamePasswordAuthenticationToken(userDetails, rawPassword, userDetails.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
