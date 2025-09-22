package dev.muho.hotel.error;

public class RatePlanNotFoundException extends RuntimeException {
    public RatePlanNotFoundException() { super("Rate plan not found"); }
    public RatePlanNotFoundException(String msg) { super(msg); }
}

