package dev.muho.booking.domain.booking.client;

import java.util.Optional;

public interface HotelClient {
    Optional<String> findHotelName(Long hotelId);
}

