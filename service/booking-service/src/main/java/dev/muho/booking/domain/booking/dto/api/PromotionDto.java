package dev.muho.booking.domain.booking.dto.api;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PromotionDto {
    private Long promotionId;
    private String promotionName;
    private String discountType;
    private BigDecimal discountAmount;
}
