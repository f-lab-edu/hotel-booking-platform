package dev.muho.user.dto.command;

public record AuthResult(String accessToken, String refreshToken) {

    public static AuthResult of(String accessToken, String refreshToken) {
        return new AuthResult(accessToken, refreshToken);
    }
}
