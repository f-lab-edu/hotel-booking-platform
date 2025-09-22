package dev.muho.hotel.dto.api;

import dev.muho.hotel.dto.command.RoomInventoryInfoResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RoomInventoryResponse {
    private Long id;
    private Long roomTypeId;
    private LocalDate date;
    private Integer totalRooms;
    private Integer availableRooms;

    public static RoomInventoryResponse from(RoomInventoryInfoResult r) {
        return new RoomInventoryResponse(
                r.id(),
                r.roomTypeId(),
                r.date(),
                r.totalRooms(),
                r.availableRooms()
        );
    }
}
