package dev.muho.user.domain.auth.dto.api;

import dev.muho.user.domain.auth.dto.command.AuthResult;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public static AuthResponse of(AuthResult authResult) {
        return AuthResponse.builder()
                .accessToken(authResult.accessToken())
                .refreshToken(authResult.refreshToken())
                .build();
    }
}
