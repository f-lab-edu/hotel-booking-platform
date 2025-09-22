package dev.muho.hotel.dto.command;

import dev.muho.hotel.entity.DiscountType;
import java.math.BigDecimal;

public record PromotionUpdateCommand(
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        Integer minNights,
        Integer bookingWindowMinDays,
        Integer bookingWindowMaxDays
) {}

