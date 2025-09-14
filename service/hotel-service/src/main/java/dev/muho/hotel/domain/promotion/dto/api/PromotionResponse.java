package dev.muho.hotel.domain.promotion.dto.api;

import dev.muho.hotel.domain.promotion.entity.DiscountType;
import dev.muho.hotel.domain.promotion.dto.command.PromotionInfoResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PromotionResponse {
    private Long id;
    private Long applicableHotelId;
    private Long applicableRoomTypeId;
    private Long applicableRatePlanId;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer minNights;
    private Integer bookingWindowMinDays;
    private Integer bookingWindowMaxDays;

    public static PromotionResponse from(PromotionInfoResult r) {
        return new PromotionResponse(
                r.id(),
                r.applicableHotelId(),
                r.applicableRoomTypeId(),
                r.applicableRatePlanId(),
                r.name(),
                r.description(),
                r.discountType(),
                r.discountValue(),
                r.minNights(),
                r.bookingWindowMinDays(),
                r.bookingWindowMaxDays()
        );
    }
}
