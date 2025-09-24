package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RatePlanStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private Status status;
}
