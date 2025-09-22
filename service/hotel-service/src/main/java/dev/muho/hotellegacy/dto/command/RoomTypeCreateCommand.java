package dev.muho.hotellegacy.dto.command;

public record RoomTypeCreateCommand(
        String name,
        String description,
        Integer maxOccupancy,
        Integer standardOccupancy,
        String viewType,
        String bedType
) {}

