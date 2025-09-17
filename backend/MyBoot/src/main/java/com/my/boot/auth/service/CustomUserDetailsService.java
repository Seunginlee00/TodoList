package com.my.boot.auth.service;

import com.my.boot.user.dto.UserDTO;
import com.my.boot.user.entity.User;
import com.my.boot.user.repository.UserRepository;
import com.my.boot.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
    private final EncryptService encryptService;


    @Override
  public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
    log.info("----------------loadUserByUsername-----------------------------");

    User user = userRepository.selectUserWithRoles(loginId).orElseThrow(
        () ->  new UsernameNotFoundException("사용자 없음"));


        List<String> roleNames = userRoleRepository.findRoleCsvByUserId(loginId)
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted(Comparator.comparingInt((String s) ->
                        switch (s) {
                            case "USER" -> 1;
                            case "MANAGER" -> 2;
                            case "ADMIN" -> 3;
                            default -> 4;
                        }).thenComparing(s -> s))
                .toList();

    // 해당 부분 변환 필요
    // dto > EncryptService > MockSafeDb 가져오는 작업이 필요 ~


        UserDTO userDTO = new UserDTO(
        user.getLoginId(),
        user.getPasswd(),
        user.getSalt(),
        user.getUserNm(),
        roleNames,
        user.getPasswdChangeDate(),
        encryptService // 🔥 복호화 서비스 전달
    );

    log.info("✅ 사용자 인증 성공: userId={}, userNm={}, roles={}",
        userDTO.getUserId(), userDTO.getUserNm(), roleNames);

    return userDTO;
  }
}
