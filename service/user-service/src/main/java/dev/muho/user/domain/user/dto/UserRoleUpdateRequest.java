package dev.muho.user.domain.user.dto;

import dev.muho.user.domain.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRoleUpdateRequest {

    @NotNull(message = "역할은 필수입니다.")
    private UserRole role;
}
