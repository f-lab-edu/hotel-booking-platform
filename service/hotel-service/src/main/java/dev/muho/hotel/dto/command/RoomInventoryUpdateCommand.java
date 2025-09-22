package dev.muho.hotel.dto.command;

public record RoomInventoryUpdateCommand(
        Integer totalRooms,
        Integer availableRooms
) {}

