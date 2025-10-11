package dev.muho.hotel.dto.request;

import dev.muho.hotel.global.validation.DateRangeValidatable;
import dev.muho.hotel.global.validation.StartDateBeforeOrEqualEndDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@StartDateBeforeOrEqualEndDate
public class BaseRateBulkUpdateRequest implements DateRangeValidatable {

    @NotNull(message = "요금제 ID는 필수입니다.")
    private Long ratePlanId;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    private BigDecimal price;
}
