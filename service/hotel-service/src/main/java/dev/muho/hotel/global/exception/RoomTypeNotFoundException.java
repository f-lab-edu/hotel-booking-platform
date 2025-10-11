package dev.muho.hotel.global.exception;

public class RoomTypeNotFoundException extends CustomException {
    public RoomTypeNotFoundException() {
        super(ErrorCode.ROOM_TYPE_NOT_FOUND);
    }
}
