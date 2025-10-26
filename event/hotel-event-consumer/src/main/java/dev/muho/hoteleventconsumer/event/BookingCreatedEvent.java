package dev.muho.hoteleventconsumer.event;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BookingCreatedEvent {
    private Long bookingLongId;
    private String bookingId;
    private Long hotelId;
    private Long roomTypeId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
