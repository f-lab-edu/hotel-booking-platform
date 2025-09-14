package dev.muho.booking.domain.booking.error;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException() { super("Booking not found"); }
    public BookingNotFoundException(String msg) { super(msg); }
}

