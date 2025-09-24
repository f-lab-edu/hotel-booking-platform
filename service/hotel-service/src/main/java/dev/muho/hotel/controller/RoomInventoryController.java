package dev.muho.hotel.controller;

import dev.muho.hotel.dto.request.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.dto.response.RoomInventoryResponse;
import dev.muho.hotel.service.RoomInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoomInventoryController {

    private final RoomInventoryService roomInventoryService;

    @GetMapping("/room-types/{roomTypeId}/inventories")
    public ResponseEntity<List<RoomInventoryResponse>> getRoomInventories(
            @PathVariable Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RoomInventoryResponse> inventories = roomInventoryService.getRoomInventories(roomTypeId, startDate, endDate);
        return ResponseEntity.ok(inventories);
    }

    @PutMapping("/inventories/bulk-update")
    public ResponseEntity<Void> bulkUpdateRoomInventories(@Valid @RequestBody RoomInventoryBulkUpdateRequest request) {
        roomInventoryService.bulkUpdate(request);
        return ResponseEntity.noContent().build();
    }
}
