package dev.muho.hotel.global.exception;

public class RatePlanNotFoundException extends CustomException {
    public RatePlanNotFoundException() {
        super(ErrorCode.RATE_PLAN_NOT_FOUND);
    }
}
