package dev.muho.hotellegacy.dto.command;

import dev.muho.hotellegacy.entity.DiscountType;
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

