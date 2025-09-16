package dev.muho.user.dto.command;

import dev.muho.user.dto.api.UserProfileUpdateRequest;

public record UserProfileUpdateCommand(String name, String phone) {
    public static UserProfileUpdateCommand from(UserProfileUpdateRequest request) {
        return new UserProfileUpdateCommand(
                request.getName(),
                request.getPhone()
        );
    }
}
