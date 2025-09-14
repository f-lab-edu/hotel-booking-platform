package dev.muho.booking.domain.booking.dto.api;

import dev.muho.booking.domain.booking.dto.command.BookingInfoResult;
import dev.muho.booking.domain.booking.entity.BookingStatus;
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

    public static BookingResponse from(BookingInfoResult r) {
        return BookingResponse.builder()
                .bookingId(r.bookingId())
                .status(r.status())
                .userId(r.userId())
                .guestName(r.guestName())
                .hotelId(r.hotelId())
                .hotelName(r.hotelName())
                .roomTypeName(r.roomTypeName())
                .ratePlanName(r.ratePlanName())
                .checkInDate(r.checkInDate())
                .checkOutDate(r.checkOutDate())
                .guestCount(r.guestCount())
                .basePrice(r.basePrice())
                .finalPrice(r.finalPrice())
                // promotions/rateAdjustments are not stored in Booking entity currently
                .promotions(null)
                .rateAdjustments(null)
                .build();
    }
}
