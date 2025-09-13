package dev.muho.user.domain.auth.dto.command;

/**
 * 로그인 요청 커맨드 (이메일/비밀번호).
 */
public record AuthLoginCommand(
        String email,
        String password
) {
    public AuthLoginCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email 은 비어있을 수 없습니다.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password 는 비어있을 수 없습니다.");
    }

    public static AuthLoginCommand of(String email, String password) {
        return new AuthLoginCommand(email, password);
    }
}

