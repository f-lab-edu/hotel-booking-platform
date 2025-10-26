package dev.muho.booking.domain.booking.event;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public class BookingCreatedEvent {
    private Long bookingLongId;
    private String bookingId;
    private Long hotelId;
    private Long roomTypeId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
