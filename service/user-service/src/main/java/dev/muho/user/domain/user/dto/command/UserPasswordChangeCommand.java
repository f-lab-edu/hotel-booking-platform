package dev.muho.user.domain.user.dto.command;

public record UserPasswordChangeCommand(String currentPassword, String newPassword) {
}

