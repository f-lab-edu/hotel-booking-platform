package dev.muho.user.dto.command;

import dev.muho.user.dto.api.LoginRequest;

/**
 * 로그인 요청 커맨드 (이메일/비밀번호).
 */
public record AuthLoginCommand(
        String email,
        String password
) {
    public static AuthLoginCommand from(LoginRequest request) {
        return new AuthLoginCommand(request.getEmail(), request.getPassword());
    }
}

