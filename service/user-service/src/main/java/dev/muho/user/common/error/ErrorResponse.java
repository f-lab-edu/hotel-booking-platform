package dev.muho.user.common.error;

import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        String detail,
        Map<String, String> errors // validation 에러용
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null, null);
    }
    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), detail, null);
    }
    public static ErrorResponse validation(Map<String, String> errors) {
        return new ErrorResponse(ErrorCode.INVALID_API_INPUT.getCode(), ErrorCode.INVALID_API_INPUT.getMessage(), null, errors);
    }
}
