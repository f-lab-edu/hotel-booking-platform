package dev.muho.user.common.error;

public record ErrorResponse(String code, String message, String detail) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }
    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), detail);
    }
}
