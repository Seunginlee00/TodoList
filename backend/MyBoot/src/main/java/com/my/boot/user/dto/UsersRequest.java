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

  public User toEntity (String encodePasswd, String salt) {
    return User.builder()
        .loginId(loginId())
        .userNm(userNm())
        .email(email())
        .salt(salt)
        .mobileNo(mobileNo())
        .passwd(encodePasswd)
        .build();
  }


}