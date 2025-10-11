package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RatePlanSearchRequest {

    @NotNull(message = "호텔 ID는 필수입니다.")
    private Long hotelId;

    private Long roomTypeId;

    private String name;

    private Boolean includesBreakfast;

    private Boolean refundable;

    private Status status;

    private Integer minNights;

    private Integer maxNights;

    private LocalDate bookingStartDate;

    private LocalDate bookingEndDate;

    private LocalDate checkInStartDate;

    private LocalDate checkInEndDate;
}
