package dev.muho.hotel.dto.response;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.HotelStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotelResponse {
    private final Long hotelId;
    private final String hotelName;
    private final String address;
    private final int rating;
    private final HotelStatus status;

    public static HotelResponse from(Hotel hotel) {
        return HotelResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .address(hotel.getAddress())
                .rating(hotel.getRating())
                .status(hotel.getStatus())
                .build();
    }
}
