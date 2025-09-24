package dev.muho.hotel.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RoomInventoryBulkUpdateRequest {

    @NotNull(message = "객실 유형 ID는 필수입니다.")
    private Long roomTypeId;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "총 재고 수량은 필수입니다.")
    private Integer totalQuantity;
}
