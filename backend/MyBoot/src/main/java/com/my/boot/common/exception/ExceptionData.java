package com.my.boot.common.exception;


import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ExceptionData {

    // System Exception
    RUNTIME_EXCEPTION(HttpStatus.BAD_REQUEST, "E0001"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED, "E0002"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "E0003"),
    ILLEGAL_ARGUMENT_ERROR(HttpStatus.BAD_REQUEST, "E0004", "필수 파라미터가 없습니다"),
    ID_PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "E0005",
        "아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요."),


    // Custom Exception
    SECURITY_EXCEPTION(HttpStatus.UNAUTHORIZED, "CE0001", "인가받지 않은 회원입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "CE0002", "회원을 조회할 수 없습니다."),
    EXISTS_USER(HttpStatus.BAD_REQUEST, "CE0003", "이미 존재하는 아이디 입니다."),

    // 게시판 관련
    NOT_FOUND_BOARD(HttpStatus.NOT_FOUND, "CE0004", "해당 게시글을 찾을 수 없습니다." ),
    NOT_FOUND_BOARD_CONFIG(HttpStatus.NOT_FOUND, "CE0005", "해당 게시글 설정을 찾을 수 없습니다." )
    ;


    private final HttpStatus status;
    private final String code;
    private String message;

    ExceptionData(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
    }

    ExceptionData(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
