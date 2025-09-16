package dev.muho.user.dto.command;

public record UserPasswordChangeCommand(String currentPassword, String newPassword) {
}

