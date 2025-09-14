package dev.muho.hotel.domain.roominventory.dto.command;

import dev.muho.hotel.domain.roominventory.entity.RoomInventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RoomInventoryInfoResult(
        Long id,
        Long roomTypeId,
        LocalDate date,
        Integer totalRooms,
        Integer availableRooms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RoomInventoryInfoResult from(RoomInventory r) {
        return new RoomInventoryInfoResult(
                r.getId(),
                r.getRoomTypeId(),
                r.getDate(),
                r.getTotalRooms(),
                r.getAvailableRooms(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

