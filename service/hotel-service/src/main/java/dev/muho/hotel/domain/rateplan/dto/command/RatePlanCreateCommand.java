package dev.muho.hotel.domain.rateplan.dto.command;

import java.math.BigDecimal;

public record RatePlanCreateCommand(
        String name,
        String description,
        BigDecimal basePrice,
        boolean breakfastIncluded,
        boolean refundable,
        Integer minNights,
        Integer maxNights
) {}

