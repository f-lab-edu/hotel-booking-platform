package dev.muho.hotel.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AvailableRoomTypeDto {

    private final Long roomTypeId;
    private final String roomTypeName;
    private final int standardCapacity;
    private final int maxCapacity;
    private final int remainingRooms;

    private final List<AvailableRatePlanDto> availableRatePlans;
}
