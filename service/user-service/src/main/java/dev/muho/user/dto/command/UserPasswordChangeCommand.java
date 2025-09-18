package dev.muho.user.dto.command;

import dev.muho.user.dto.api.UserPasswordChangeRequest;
import jakarta.validation.constraints.NotBlank;

public record UserPasswordChangeCommand(
    @NotBlank
    String currentPassword,

    @NotBlank
    String newPassword
) {
    public static UserPasswordChangeCommand from(UserPasswordChangeRequest request) {
        return new UserPasswordChangeCommand(request.getCurrentPassword(), request.getNewPassword());
    }
}
