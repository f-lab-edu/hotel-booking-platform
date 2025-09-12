package dev.muho.user.common.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail; // 세부 메시지 (필드별 설명)

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail == null || detail.isBlank() ? errorCode.getMessage() : errorCode.getMessage() + " - " + detail);
        this.errorCode = errorCode;
        this.detail = (detail == null || detail.isBlank()) ? null : detail;
    }
}
