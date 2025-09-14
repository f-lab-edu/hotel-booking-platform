package dev.muho.hotel.domain.roomtype.dto.command;

import dev.muho.hotel.domain.roomtype.entity.RoomType;
import java.time.LocalDateTime;

public record RoomTypeInfoResult(
        Long id,
        Long hotelId,
        String name,
        String description,
        Integer maxOccupancy,
        Integer standardOccupancy,
        String viewType,
        String bedType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RoomTypeInfoResult from(RoomType r) {
        return new RoomTypeInfoResult(
                r.getId(),
                r.getHotelId(),
                r.getName(),
                r.getDescription(),
                r.getMaxOccupancy(),
                r.getStandardOccupancy(),
                r.getViewType(),
                r.getBedType(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

