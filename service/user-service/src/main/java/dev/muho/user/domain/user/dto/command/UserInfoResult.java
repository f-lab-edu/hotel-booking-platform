package dev.muho.user.domain.user.dto.command;

import dev.muho.user.domain.user.entity.User;
import dev.muho.user.domain.user.entity.UserRole;
import dev.muho.user.domain.user.entity.UserStatus;

import java.time.LocalDateTime;

public record UserInfoResult(
        Long id,
        String email,
        String name,
        String phone,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserInfoResult from(User user) {
        return new UserInfoResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
