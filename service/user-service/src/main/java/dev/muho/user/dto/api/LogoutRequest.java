package dev.muho.user.dto.api;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutRequest {

    @NotNull(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
