package dev.muho.user.dto.command;

import dev.muho.user.dto.api.TokenRefreshRequest;

public record TokenRefreshCommand(String refreshToken) {
    public static TokenRefreshCommand from(TokenRefreshRequest request) {
        return new TokenRefreshCommand(request.getRefreshToken());
    }
}
