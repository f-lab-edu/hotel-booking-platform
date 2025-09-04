package dev.muho.hotel.domain.ratecalendar.dto;

import dev.muho.hotel.domain.ratecalendar.AdjustmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
public class RateCalendarCreateRequest {

    @NotBlank(message = "규칙 이름은 필수입니다.")
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Set<DayOfWeek> applicableDays;

    @NotNull(message = "조정 유형은 필수입니다.")
    private AdjustmentType adjustmentType;

    @NotNull(message = "조정 값은 필수입니다.")
    @Positive(message = "조정 값은 양수여야 합니다.")
    private BigDecimal adjustmentValue;
}
