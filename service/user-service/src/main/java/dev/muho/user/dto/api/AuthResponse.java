package dev.muho.user.dto.api;

import dev.muho.user.dto.command.AuthResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public static AuthResponse from(AuthResult authResult) {
        return new AuthResponse(authResult.accessToken(), authResult.refreshToken());
    }
}
