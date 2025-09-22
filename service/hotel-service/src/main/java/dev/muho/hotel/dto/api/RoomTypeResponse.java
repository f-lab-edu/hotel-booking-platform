package dev.muho.hotel.dto.api;

import dev.muho.hotel.dto.command.RoomTypeInfoResult;
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

    public static RoomTypeResponse from(RoomTypeInfoResult r) {
        return new RoomTypeResponse(
                r.id(),
                r.hotelId(),
                r.name(),
                r.description(),
                r.maxOccupancy(),
                r.standardOccupancy(),
                r.viewType(),
                r.bedType()
        );
    }
}
