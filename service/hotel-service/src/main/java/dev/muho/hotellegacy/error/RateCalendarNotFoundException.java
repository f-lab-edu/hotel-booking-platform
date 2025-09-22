package dev.muho.hotellegacy.error;

public class RateCalendarNotFoundException extends RuntimeException {
    public RateCalendarNotFoundException() { super("Rate calendar not found"); }
    public RateCalendarNotFoundException(String msg) { super(msg); }
}

