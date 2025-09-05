package dev.muho.hotel.domain.roominventory.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RoomInventoryUpdateRequest {

    @NotNull(message = "날짜는 필수입니다.")
    @FutureOrPresent(message = "날짜는 오늘이거나 오늘 이후여야 합니다.")
    private LocalDate date;

    @NotNull(message = "전체 객실 수는 필수입니다.")
    @PositiveOrZero(message = "전체 객실 수는 0 이상이어야 합니다.")
    private Integer totalRooms;

    @NotNull(message = "예약 가능 객실 수는 필수입니다.")
    @PositiveOrZero(message = "예약 가능 객실 수는 0 이상이어야 합니다.")
    private Integer availableRooms;
}
