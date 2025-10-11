package dev.muho.hotel.global.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {
    private final int status;
    private final String message;
    private final Map<String, String> errors; // validation 에러용

    // 기본 생성자 (Jackson 역직렬화용)
    public ErrorResponse() {
        this.status = 0;
        this.message = null;
        this.errors = null;
    }

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.errors = null;
    }

    public ErrorResponse(ErrorCode errorCode, Map<String, String> errors) {
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }
}
