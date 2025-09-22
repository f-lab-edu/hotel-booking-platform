package dev.muho.hotellegacy.controller;

import dev.muho.hotellegacy.dto.api.PromotionCreateRequest;
import dev.muho.hotellegacy.dto.command.RoomInventoryBulkUpdateCommand;
import dev.muho.hotellegacy.dto.command.RoomInventoryUpdateCommand;
import dev.muho.hotellegacy.service.RoomInventoryService;
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
    public Page<PromotionCreateRequest.RoomInventoryResponse> getInventories(@PathVariable Long roomTypeId, Pageable pageable) {
        return service.search(roomTypeId, pageable).map(PromotionCreateRequest.RoomInventoryResponse::from);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkUpdateInventory(@PathVariable Long roomTypeId, @Valid @RequestBody PromotionCreateRequest.RoomInventoryBulkUpdateRequest roomInventoryBulkUpdateRequest) {
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
    public void updateInventory(@PathVariable Long roomTypeId, @PathVariable LocalDate date, @Valid @RequestBody PromotionCreateRequest.RoomInventoryUpdateRequest roomInventoryUpdateRequest) {
        RoomInventoryUpdateCommand cmd = new RoomInventoryUpdateCommand(
                roomInventoryUpdateRequest.getTotalRooms(),
                roomInventoryUpdateRequest.getAvailableRooms()
        );
        service.update(roomTypeId, date, cmd);
    }
}
