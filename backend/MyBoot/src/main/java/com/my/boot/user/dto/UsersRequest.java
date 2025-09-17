package com.my.boot.user.dto;


import com.my.boot.user.entity.User;

public record UsersRequest(
    Long id,
    String loginId,
    String passwd,
    String userNm,
    String email,
    String mobileNo,
    int access,
    int display
) {

  public User toEntity (String encodePasswd) {
    return User.builder()
        .loginId(loginId())
        .userNm(userNm())
        .email(email())
        .mobileNo(mobileNo())
        .passwd(encodePasswd)
        .build();
  }


}