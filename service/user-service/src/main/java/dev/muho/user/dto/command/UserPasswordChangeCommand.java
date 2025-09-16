package dev.muho.user.dto.command;

import dev.muho.user.dto.api.UserPasswordChangeRequest;

public record UserPasswordChangeCommand(String currentPassword, String newPassword) {
    public static UserPasswordChangeCommand from(UserPasswordChangeRequest request) {
        return new UserPasswordChangeCommand(request.getCurrentPassword(), request.getNewPassword());
    }
}

