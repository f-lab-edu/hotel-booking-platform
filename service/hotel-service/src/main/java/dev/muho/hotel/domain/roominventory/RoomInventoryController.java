package dev.muho.hotel.domain.roominventory;

import dev.muho.hotel.domain.roominventory.dto.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryResponse;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/room-types/{roomTypeId}/inventories")
@RequiredArgsConstructor
public class RoomInventoryController {

    private final FakeRoomInventoryRepository roomInventoryRepository;

    @GetMapping
    public Page<RoomInventoryResponse> getInventories(@PathVariable Long roomTypeId, Pageable pageable) {
        return roomInventoryRepository.findAll(roomTypeId, pageable);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkUpdateInventory(@PathVariable Long roomTypeId, @Valid @RequestBody RoomInventoryBulkUpdateRequest roomInventoryBulkUpdateRequest) {
        roomInventoryRepository.bulkUpdate(roomTypeId, roomInventoryBulkUpdateRequest);
    }

    @PutMapping("/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInventory(@PathVariable Long roomTypeId, @PathVariable LocalDate date, @Valid @RequestBody RoomInventoryUpdateRequest roomInventoryUpdateRequest) {
        roomInventoryRepository.update(roomTypeId, date, roomInventoryUpdateRequest);
    }
}
