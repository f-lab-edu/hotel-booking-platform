package dev.muho.user.domain.auth.error;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;

/**
 * 인증 실패 (이메일 없음 / 비밀번호 불일치 / 비활성/탈퇴 사용자 등 포함) 예외.
 * 보안 상 구체 사유는 노출하지 않음.
 */
public class AuthenticationFailedException extends BusinessException {
    public AuthenticationFailedException() {
        super(ErrorCode.AUTHENTICATION_FAILED);
    }
}

