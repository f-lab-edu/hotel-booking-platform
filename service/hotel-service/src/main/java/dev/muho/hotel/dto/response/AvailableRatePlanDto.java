package dev.muho.hotel.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AvailableRatePlanDto {
    private final Long ratePlanId;
    private final String ratePlanName;
    private final BigDecimal totalPrice; // 해당 요금제의 최종 계산된 총액
}
