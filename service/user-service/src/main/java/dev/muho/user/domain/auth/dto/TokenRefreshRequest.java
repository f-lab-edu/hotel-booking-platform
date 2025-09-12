package dev.muho.user.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshRequest {
    @NotNull(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
