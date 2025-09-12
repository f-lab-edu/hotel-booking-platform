package dev.muho.user.domain.user.dto.command;

import dev.muho.user.domain.user.entity.UserRole;
import dev.muho.user.domain.user.entity.UserStatus;
import java.time.LocalDateTime;

public record UserSearchCondition(
        UserRole role,
        UserStatus status,
        String keyword // email 또는 name 포함 검색 용도
) {
    public boolean hasKeyword() { return keyword != null && !keyword.isBlank(); }
    public boolean hasRole() { return role != null; }
    public boolean hasStatus() { return status != null; }
}

