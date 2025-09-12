package dev.muho.hotel.domain.promotion.dto;

import dev.muho.hotel.domain.promotion.DiscountType;
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
}
