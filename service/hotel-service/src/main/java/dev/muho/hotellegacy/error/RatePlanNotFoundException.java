package dev.muho.hotellegacy.error;

public class RatePlanNotFoundException extends RuntimeException {
    public RatePlanNotFoundException() { super("Rate plan not found"); }
    public RatePlanNotFoundException(String msg) { super(msg); }
}

