package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.AdjustmentType;
import dev.muho.hotel.domain.CalculationType;
import dev.muho.hotel.domain.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PriceAdjustmentSearchRequest {

    @NotNull(message = "호텔 ID는 필수입니다.")
    private Long hotelId;

    private Long roomTypeId;

    private Long ratePlanId;

    private String name;

    private AdjustmentType adjustmentType;

    private CalculationType calculationType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Status status;

    private Integer bookingDaysBeforeArrival;
}
