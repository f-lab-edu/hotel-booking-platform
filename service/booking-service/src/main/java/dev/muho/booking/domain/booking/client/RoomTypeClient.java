package dev.muho.booking.domain.booking.client;

import java.util.Optional;

public interface RoomTypeClient {
    Optional<String> findRoomTypeName(Long roomTypeId);
}

