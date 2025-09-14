package dev.muho.hotel.domain.roominventory.dto.command;

public record RoomInventoryUpdateCommand(
        Integer totalRooms,
        Integer availableRooms
) {}

