package dev.muho.hotellegacy.dto.command;

import dev.muho.hotellegacy.entity.DiscountType;
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

