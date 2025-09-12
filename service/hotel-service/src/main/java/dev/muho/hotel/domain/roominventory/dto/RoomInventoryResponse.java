package dev.muho.hotel.domain.roominventory.dto;

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
}
