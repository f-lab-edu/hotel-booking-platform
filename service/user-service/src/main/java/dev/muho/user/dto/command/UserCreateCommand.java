package dev.muho.user.dto.command;

import dev.muho.user.dto.api.UserSignupRequest;

public record UserCreateCommand(String email, String rawPassword, String name, String phone) {
    public static UserCreateCommand from(UserSignupRequest request) {
        return new UserCreateCommand(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getPhone()
        );
    }
}
