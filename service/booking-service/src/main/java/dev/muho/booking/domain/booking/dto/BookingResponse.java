package dev.muho.booking.domain.booking.dto;

import dev.muho.booking.domain.booking.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BookingResponse {

    private String bookingId; // UUID 등 고유 식별자
    private BookingStatus status;

    // --- User Info ---
    private Long userId;
    private String guestName;

    // --- Hotel Info ---
    private Long hotelId;
    private String hotelName;
    private String roomTypeName;
    private String ratePlanName;


    // --- Booking Details ---
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private List<PromotionDto> promotions;
    private List<RateCalendarDto> rateAdjustments;
}
