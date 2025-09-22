package dev.muho.hotellegacy.dto.command;

public record HotelUpdateCommand(
        String name,
        String address,
        String country,
        String city,
        Integer rating,
        String description,
        String contactNumber
) {}

