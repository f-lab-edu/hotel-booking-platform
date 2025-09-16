package dev.muho.user.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // api
    INVALID_API_INPUT(HttpStatus.BAD_REQUEST, "API001", "입력값이 유효하지 않습니다."),

    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "U002", "접근이 거부되었습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U003", "이미 존재하는 사용자입니다."),
    INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "U004", "잘못된 사용자 입력입니다."),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "U005", "잘못된 사용자 역할입니다."),

    // auth
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다.");

    // field
    private final HttpStatus status;
    private final String code;
    private final String message;
}
