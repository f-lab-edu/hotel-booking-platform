package dev.muho.hotel.domain.hotel.error;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException() {
        super("호텔을 찾을 수 없습니다.");
    }

    public HotelNotFoundException(String message) {
        super(message);
    }
}

