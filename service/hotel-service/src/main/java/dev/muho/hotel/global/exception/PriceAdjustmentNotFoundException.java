package dev.muho.hotel.global.exception;

public class PriceAdjustmentNotFoundException extends CustomException {

    public PriceAdjustmentNotFoundException() {
        super(ErrorCode.PRICE_ADJUSTMENT_NOT_FOUND);
    }
}
