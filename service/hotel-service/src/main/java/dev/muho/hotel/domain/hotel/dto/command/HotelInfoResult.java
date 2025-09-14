package dev.muho.hotel.domain.hotel.dto.command;

import dev.muho.hotel.domain.hotel.entity.Hotel;

import java.time.LocalDateTime;

public record HotelInfoResult(
        Long id,
        String name,
        String address,
        String country,
        String city,
        Integer rating,
        String description,
        String contactNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static HotelInfoResult from(Hotel hotel) {
        return new HotelInfoResult(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCountry(),
                hotel.getCity(),
                hotel.getRating(),
                hotel.getDescription(),
                hotel.getContactNumber(),
                hotel.getCreatedAt(),
                hotel.getUpdatedAt()
        );
    }
}

