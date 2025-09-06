package dev.muho.booking.domain.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BookingRequest {

    @NotNull
    private Long hotelId;

    @NotNull
    private Long roomTypeId;

    @NotNull
    private Long ratePlanId;

    @NotNull
    @FutureOrPresent(message = "체크인 날짜는 오늘 이후여야 합니다.")
    private LocalDate checkInDate;

    @NotNull
    @FutureOrPresent(message = "체크아웃 날짜는 오늘 이후여야 합니다.")
    private LocalDate checkOutDate;

    @NotNull
    @Positive
    private Integer guestCount;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    @NotNull
    @Positive
    private BigDecimal finalPrice;

    private List<PromotionDto> promotions;

    private List<RateCalendarDto> rateAdjustments;
}
