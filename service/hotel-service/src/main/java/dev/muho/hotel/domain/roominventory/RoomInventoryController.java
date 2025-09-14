package dev.muho.hotel.domain.roominventory;

import dev.muho.hotel.domain.roominventory.dto.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryResponse;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryUpdateRequest;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryBulkUpdateCommand;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryUpdateCommand;
import dev.muho.hotel.domain.roominventory.service.RoomInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/room-types/{roomTypeId}/inventories")
@RequiredArgsConstructor
public class RoomInventoryController {

    private final RoomInventoryService service;

    @GetMapping
    public Page<RoomInventoryResponse> getInventories(@PathVariable Long roomTypeId, Pageable pageable) {
        return service.search(roomTypeId, pageable).map(RoomInventoryResponse::from);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkUpdateInventory(@PathVariable Long roomTypeId, @Valid @RequestBody RoomInventoryBulkUpdateRequest roomInventoryBulkUpdateRequest) {
        RoomInventoryBulkUpdateCommand cmd = new RoomInventoryBulkUpdateCommand(
                roomInventoryBulkUpdateRequest.getStartDate(),
                roomInventoryBulkUpdateRequest.getEndDate(),
                roomInventoryBulkUpdateRequest.getTotalRooms(),
                roomInventoryBulkUpdateRequest.getAvailableRooms()
        );
        service.bulkUpdate(roomTypeId, cmd);
    }

    @PutMapping("/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInventory(@PathVariable Long roomTypeId, @PathVariable LocalDate date, @Valid @RequestBody RoomInventoryUpdateRequest roomInventoryUpdateRequest) {
        RoomInventoryUpdateCommand cmd = new RoomInventoryUpdateCommand(
                roomInventoryUpdateRequest.getTotalRooms(),
                roomInventoryUpdateRequest.getAvailableRooms()
        );
        service.update(roomTypeId, date, cmd);
    }
}
