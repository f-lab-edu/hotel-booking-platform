package dev.muho.hotel.error;

public class RateCalendarNotFoundException extends RuntimeException {
    public RateCalendarNotFoundException() { super("Rate calendar not found"); }
    public RateCalendarNotFoundException(String msg) { super(msg); }
}

