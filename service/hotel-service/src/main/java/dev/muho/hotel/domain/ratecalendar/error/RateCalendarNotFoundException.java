package dev.muho.hotel.domain.ratecalendar.error;

public class RateCalendarNotFoundException extends RuntimeException {
    public RateCalendarNotFoundException() { super("Rate calendar not found"); }
    public RateCalendarNotFoundException(String msg) { super(msg); }
}

