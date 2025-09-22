package dev.muho.hotel.dto.command;

public record HotelCreateCommand(
        String name,
        String address,
        String country,
        String city,
        Integer rating,
        String description,
        String contactNumber
) {}

