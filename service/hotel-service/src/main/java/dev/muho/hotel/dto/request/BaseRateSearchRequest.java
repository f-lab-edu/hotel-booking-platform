package dev.muho.hotel.dto.request;

import dev.muho.hotel.global.validation.DateRangeValidatable;
import dev.muho.hotel.global.validation.StartDateBeforeOrEqualEndDate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@StartDateBeforeOrEqualEndDate
public class BaseRateSearchRequest implements DateRangeValidatable {

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;
}
