package dev.muho.hotel.domain.roomtype.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomTypeResponse {
    private Long id;
    private Long hotelId;
    private String name;
    private String description;
    private Integer maxOccupancy;
    private Integer standardOccupancy;
    private String viewType;
    private String bedType;
}
