package dev.muho.hotel.dto.response;

import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomTypeResponse {

    private Long id;
    private Long hotelId;
    private String name;
    private int standardCapacity;
    private int maxCapacity;
    private Status status;

    public static RoomTypeResponse from(RoomType roomType) {
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .hotelId(roomType.getHotel().getId())
                .name(roomType.getName())
                .standardCapacity(roomType.getStandardCapacity())
                .maxCapacity(roomType.getMaxCapacity())
                .status(roomType.getStatus())
                .build();
    }
}
