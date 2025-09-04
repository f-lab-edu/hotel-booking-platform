package dev.muho.hotel.domain.roominventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomInventoryResponse {
    private Long id;
    private String date;
    private Integer totalRooms;
    private Integer availableRooms;
}
