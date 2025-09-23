package dev.muho.hotel.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- Hotel ---
    HOTEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 호텔입니다."),

    // --- Rate ---
    BASE_RATE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "해당 날짜({0})의 기본 요금 설정을 찾을 수 없습니다."),

    // --- Common ---
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 파라미터의 타입이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
