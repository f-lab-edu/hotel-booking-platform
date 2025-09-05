package dev.muho.hotel.domain.rateplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RatePlanUpdateRequest {

    @NotBlank(message = "요금제 이름은 필수입니다.")
    private String name;

    private String description;

    @NotNull(message = "기본 가격은 필수입니다.")
    @PositiveOrZero(message = "기본 가격은 0 이상이어야 합니다.")
    private BigDecimal basePrice;

    private boolean breakfastIncluded;

    private boolean refundable;

    @Positive(message = "최소 숙박일은 1 이상이어야 합니다.")
    private Integer minNights;

    @Positive(message = "최대 숙박일은 1 이상이어야 합니다.")
    private Integer maxNights;
}
