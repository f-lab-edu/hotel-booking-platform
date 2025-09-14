package dev.muho.booking.domain.booking.dto.api;

import dev.muho.booking.domain.booking.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingStatusUpdateRequest {

    @NotNull(message = "변경할 상태는 필수입니다.")
    private BookingStatus status;
}
