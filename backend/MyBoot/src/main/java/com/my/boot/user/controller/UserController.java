package com.my.boot.user.controller;

import com.my.boot.user.dto.UsersRequest;
import com.my.boot.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 API")
@RequestMapping("/api/user")
@RequiredArgsConstructor
@RestController
@Slf4j
public class UserController {
  private final UserService userService;
  /* 회원 가입*/
  @Operation(summary = "회원 가입하기")
  @PostMapping("/register")
  public ResponseEntity<Object> userJoin(@RequestBody UsersRequest request) {
    return ResponseEntity.ok(userService.userSignUp(request));
  }

}
