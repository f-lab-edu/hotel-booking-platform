package dev.muho.hotel.global.exception;

public class HotelNotFoundException extends CustomException {
    public HotelNotFoundException() {
        super(ErrorCode.HOTEL_NOT_FOUND);
    }
}
