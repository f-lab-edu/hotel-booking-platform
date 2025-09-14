package dev.muho.booking.domain.booking.dto.command;

import dev.muho.booking.domain.booking.dto.api.BookingRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookingCreateCommand(
        Long hotelId,
        Long roomTypeId,
        Long ratePlanId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer guestCount,
        BigDecimal basePrice,
        BigDecimal finalPrice
) {
    public static BookingCreateCommand from(BookingRequest req) {
        return new BookingCreateCommand(
                req.getHotelId(),
                req.getRoomTypeId(),
                req.getRatePlanId(),
                req.getCheckInDate(),
                req.getCheckOutDate(),
                req.getGuestCount(),
                req.getBasePrice(),
                req.getFinalPrice()
        );
    }
}

