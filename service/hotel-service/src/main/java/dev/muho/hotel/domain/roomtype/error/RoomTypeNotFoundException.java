package dev.muho.hotel.domain.roomtype.error;

public class RoomTypeNotFoundException extends RuntimeException {
    public RoomTypeNotFoundException() { super("Room type not found"); }
    public RoomTypeNotFoundException(String msg) { super(msg); }
}

