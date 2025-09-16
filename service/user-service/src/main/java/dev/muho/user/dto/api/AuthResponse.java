package dev.muho.user.dto.api;

import dev.muho.user.dto.command.AuthResult;
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
