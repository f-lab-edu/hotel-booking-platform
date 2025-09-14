package dev.muho.hotel.domain.promotion.dto.api;

import dev.muho.hotel.domain.promotion.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PromotionUpdateRequest {

    private Long applicableHotelId;
    private Long applicableRoomTypeId;
    private Long applicableRatePlanId;

    @NotBlank(message = "프로모션 이름은 필수입니다.")
    private String name;

    private String description;

    @NotNull(message = "할인 유형은 필수입니다.")
    private DiscountType discountType;

    @NotNull(message = "할인 값은 필수입니다.")
    @Positive(message = "할인 값은 양수여야 합니다.")
    private BigDecimal discountValue;

    private Integer minNights;
    private Integer bookingWindowMinDays;
    private Integer bookingWindowMaxDays;
}
