package dev.muho.hotel.dto.response;

import dev.muho.hotel.domain.AdjustmentType;
import dev.muho.hotel.domain.CalculationType;
import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.Status;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PriceAdjustmentResponse {

    private Long id;
    private Long ratePlanId;
    private String name;
    private AdjustmentType adjustmentType;
    private CalculationType calculationType;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status status;
    private Integer bookingDaysBeforeArrival;

    public static PriceAdjustmentResponse from(PriceAdjustment priceAdjustment) {
        return PriceAdjustmentResponse.builder()
                .id(priceAdjustment.getId())
                .ratePlanId(priceAdjustment.getRatePlan().getId())
                .name(priceAdjustment.getName())
                .adjustmentType(priceAdjustment.getAdjustmentType())
                .calculationType(priceAdjustment.getCalculationType())
                .amount(priceAdjustment.getAmount())
                .startDate(priceAdjustment.getStartDate())
                .endDate(priceAdjustment.getEndDate())
                .status(priceAdjustment.getStatus())
                .bookingDaysBeforeArrival(priceAdjustment.getBookingDaysBeforeArrival())
                .build();
    }
}
