package dev.muho.hotel.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class AvailabilityResponse {

    private final Long hotelId;
    private final String hotelName;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;

    /**
     * 예약 가능한 객실 타입의 목록입니다.
     * 만약 예약 가능한 객실이 없다면 이 리스트는 비어있을 것입니다.
     */
    private final List<AvailableRoomTypeDto> availableRoomTypes;

    @Builder
    public AvailabilityResponse(Long hotelId,
                                String hotelName,
                                LocalDate checkInDate,
                                LocalDate checkOutDate,
                                List<AvailableRoomTypeDto> availableRoomTypes) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.availableRoomTypes = availableRoomTypes;
    }
}
