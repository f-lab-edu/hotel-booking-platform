package dev.muho.hotel.dto.response;

import dev.muho.hotel.domain.RoomInventory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RoomInventoryResponse {

    private final LocalDate date;
    private final int totalQuantity;
    private final int reservedQuantity;
    private final int availableQuantity;

    public static RoomInventoryResponse from(RoomInventory inventory) {
        return RoomInventoryResponse.builder()
                .date(inventory.getDate())
                .totalQuantity(inventory.getTotalQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getTotalQuantity() - inventory.getReservedQuantity())
                .build();
    }
}
