package dev.muho.hotel.dto.command;

import dev.muho.hotel.entity.DiscountType;
import java.math.BigDecimal;

public record PromotionCreateCommand(
        Long applicableHotelId,
        Long applicableRoomTypeId,
        Long applicableRatePlanId,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        Integer minNights,
        Integer bookingWindowMinDays,
        Integer bookingWindowMaxDays
) {}

