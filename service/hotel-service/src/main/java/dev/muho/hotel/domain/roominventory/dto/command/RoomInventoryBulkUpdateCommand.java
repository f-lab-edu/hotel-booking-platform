package dev.muho.hotel.domain.roominventory.dto.command;

import java.time.LocalDate;

public record RoomInventoryBulkUpdateCommand(
        LocalDate startDate,
        LocalDate endDate,
        Integer totalRooms,
        Integer availableRooms
) {}

