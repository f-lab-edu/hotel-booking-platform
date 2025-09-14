package dev.muho.hotel.domain.rateplan.dto;

import dev.muho.hotel.domain.rateplan.dto.command.RatePlanInfoResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RatePlanResponse {
    private Long id;
    private Long roomTypeId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private boolean isBreakfastIncluded;
    private boolean isRefundable;
    private Integer minNights;
    private Integer maxNights;

    public static RatePlanResponse from(RatePlanInfoResult r) {
        return new RatePlanResponse(
                r.id(),
                r.roomTypeId(),
                r.name(),
                r.description(),
                r.basePrice(),
                r.breakfastIncluded(),
                r.refundable(),
                r.minNights(),
                r.maxNights()
        );
    }
}
