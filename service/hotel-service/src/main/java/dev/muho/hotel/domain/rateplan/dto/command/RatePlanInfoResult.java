package dev.muho.hotel.domain.rateplan.dto.command;

import dev.muho.hotel.domain.rateplan.entity.RatePlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RatePlanInfoResult(
        Long id,
        Long roomTypeId,
        String name,
        String description,
        BigDecimal basePrice,
        boolean breakfastIncluded,
        boolean refundable,
        Integer minNights,
        Integer maxNights,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RatePlanInfoResult from(RatePlan r) {
        return new RatePlanInfoResult(
                r.getId(),
                r.getRoomTypeId(),
                r.getName(),
                r.getDescription(),
                r.getBasePrice(),
                r.isBreakfastIncluded(),
                r.isRefundable(),
                r.getMinNights(),
                r.getMaxNights(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

