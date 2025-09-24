package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.AdjustmentType;
import dev.muho.hotel.domain.CalculationType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.global.validation.DateRangeValidatable;
import dev.muho.hotel.global.validation.StartDateBeforeOrEqualEndDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@StartDateBeforeOrEqualEndDate()
public class PriceAdjustmentUpdateRequest implements DateRangeValidatable {

    @NotNull(message = "호텔 ID는 필수입니다.")
    private Long ratePlanId;

    @NotBlank(message = "가격 조정 이름은 필수입니다.")
    private String name;

    @NotNull(message = "조정 유형은 필수입니다.")
    private AdjustmentType adjustmentType;

    @NotNull(message = "계산 유형은 필수입니다.")
    private CalculationType calculationType;

    @NotNull(message = "조정 금액은 필수입니다.")
    private BigDecimal amount;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "상태는 필수입니다.")
    private Status status;

    private Integer bookingDaysBeforeArrival;
}
