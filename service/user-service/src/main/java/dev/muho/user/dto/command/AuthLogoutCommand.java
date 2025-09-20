package dev.muho.user.dto.command;

import dev.muho.user.dto.api.LogoutRequest;

public record AuthLogoutCommand(String refreshToken) {
    public static AuthLogoutCommand from(LogoutRequest request) {
        return new AuthLogoutCommand(request.getRefreshToken());
    }
}
