package dev.muho.hotellegacy.dto.command;

public record RoomInventoryUpdateCommand(
        Integer totalRooms,
        Integer availableRooms
) {}

