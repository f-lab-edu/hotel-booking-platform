package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.HotelStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class HotelStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private HotelStatus status;
}
