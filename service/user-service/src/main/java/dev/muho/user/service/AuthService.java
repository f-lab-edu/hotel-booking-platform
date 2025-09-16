package dev.muho.user.service;

import dev.muho.user.dto.api.LoginRequest;
import dev.muho.user.dto.api.TokenRefreshRequest;
import dev.muho.user.dto.command.AuthLoginCommand;
import dev.muho.user.dto.command.AuthLogoutCommand;
import dev.muho.user.dto.command.AuthResult;
import dev.muho.user.dto.command.TokenRefreshCommand;
import dev.muho.user.error.AuthenticationFailedException;

/**
 * 인증 관련 유스케이스 포트.
 */
public interface AuthService {

    /**
     * 이메일/비밀번호 기반 로그인.
     * @throws AuthenticationFailedException 실패 (이메일/비밀번호 불일치, 비활성/탈퇴 사용자)
     */
    AuthResult login(AuthLoginCommand command);

    /**
     * 리프레시 토큰을 사용한 재발급 (회전 포함 가능)
     */
    AuthResult refresh(TokenRefreshCommand command);

    /**
     * 로그아웃: 서버 측에 저장된 refresh 토큰을 무효화(삭제)한다.
     * 클라이언트는 로컬에서 토큰을 제거해야 함.
     */
    void logout(AuthLogoutCommand command);
}
