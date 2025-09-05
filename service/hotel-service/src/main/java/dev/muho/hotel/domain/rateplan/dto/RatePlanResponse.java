package dev.muho.hotel.domain.rateplan.dto;

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
}
