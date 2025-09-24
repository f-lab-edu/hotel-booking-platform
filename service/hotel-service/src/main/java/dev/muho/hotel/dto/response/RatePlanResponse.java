package dev.muho.hotel.dto.response;

import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RatePlanResponse {

    private Long id;
    private Long hotelId;
    private Long roomTypeId;
    private String name;
    private boolean includesBreakfast;
    private boolean refundable;
    private Status status;
    private Integer minNights;
    private Integer maxNights;
    private LocalDate bookingStartDate;
    private LocalDate bookingEndDate;
    private LocalDate checkInStartDate;
    private LocalDate checkInEndDate;

    public static RatePlanResponse from(RatePlan ratePlan) {
        return RatePlanResponse.builder()
                .id(ratePlan.getId())
                .hotelId(ratePlan.getRoomType().getHotel().getId())
                .roomTypeId(ratePlan.getRoomType().getId())
                .name(ratePlan.getName())
                .includesBreakfast(ratePlan.isIncludesBreakfast())
                .refundable(ratePlan.isRefundable())
                .status(ratePlan.getStatus())
                .minNights(ratePlan.getMinNights())
                .maxNights(ratePlan.getMaxNights())
                .bookingStartDate(ratePlan.getBookingStartDate())
                .bookingEndDate(ratePlan.getBookingEndDate())
                .checkInStartDate(ratePlan.getCheckInStartDate())
                .checkInEndDate(ratePlan.getCheckInEndDate())
                .build();
    }
}
