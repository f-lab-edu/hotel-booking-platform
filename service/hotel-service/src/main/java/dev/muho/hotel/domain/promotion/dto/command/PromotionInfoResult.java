package dev.muho.hotel.domain.promotion.dto.command;

import dev.muho.hotel.domain.promotion.entity.Promotion;
import dev.muho.hotel.domain.promotion.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionInfoResult(
        Long id,
        Long applicableHotelId,
        Long applicableRoomTypeId,
        Long applicableRatePlanId,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        Integer minNights,
        Integer bookingWindowMinDays,
        Integer bookingWindowMaxDays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PromotionInfoResult from(Promotion p) {
        return new PromotionInfoResult(
                p.getId(),
                p.getApplicableHotelId(),
                p.getApplicableRoomTypeId(),
                p.getApplicableRatePlanId(),
                p.getName(),
                p.getDescription(),
                p.getDiscountType(),
                p.getDiscountValue(),
                p.getMinNights(),
                p.getBookingWindowMinDays(),
                p.getBookingWindowMaxDays(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

