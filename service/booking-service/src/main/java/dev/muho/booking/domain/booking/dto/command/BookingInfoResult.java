package dev.muho.booking.domain.booking.dto.command;

import dev.muho.booking.domain.booking.entity.Booking;
import dev.muho.booking.domain.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingInfoResult(
        Long id,
        String bookingId,
        BookingStatus status,
        Long userId,
        String guestName,
        Long hotelId,
        String hotelName,
        Long roomTypeId,
        String roomTypeName,
        Long ratePlanId,
        String ratePlanName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer guestCount,
        BigDecimal basePrice,
        BigDecimal finalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookingInfoResult from(Booking b) {
        return new BookingInfoResult(
                b.getId(),
                b.getBookingId(),
                b.getStatus(),
                b.getUserId(),
                b.getGuestName(),
                b.getHotelId(),
                b.getHotelName(),
                b.getRoomTypeId(),
                b.getRoomTypeName(),
                b.getRatePlanId(),
                b.getRatePlanName(),
                b.getCheckInDate(),
                b.getCheckOutDate(),
                b.getGuestCount(),
                b.getBasePrice(),
                b.getFinalPrice(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}

