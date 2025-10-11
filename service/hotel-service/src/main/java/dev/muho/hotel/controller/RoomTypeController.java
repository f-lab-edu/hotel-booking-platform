package dev.muho.hotel.controller;

import dev.muho.hotel.dto.request.RoomTypeCreateRequest;
import dev.muho.hotel.dto.request.RoomTypeSearchRequest;
import dev.muho.hotel.dto.request.RoomTypeStatusUpdateRequest;
import dev.muho.hotel.dto.request.RoomTypeUpdateRequest;
import dev.muho.hotel.dto.response.RoomTypeResponse;
import dev.muho.hotel.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<Page<RoomTypeResponse>> getRoomTypes(
            @Valid @ModelAttribute RoomTypeSearchRequest request,
            Pageable pageable) {
        Page<RoomTypeResponse> roomTypes = roomTypeService.searchRoomTypes(request, pageable);
        return ResponseEntity.ok(roomTypes);
    }

    @GetMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> getRoomType(@PathVariable Long roomTypeId) {
        RoomTypeResponse roomType = roomTypeService.findById(roomTypeId);
        return ResponseEntity.ok(roomType);
    }

    @PostMapping
    public ResponseEntity<RoomTypeResponse> createRoomType(@Valid @RequestBody RoomTypeCreateRequest request) {
        RoomTypeResponse createdRoomType = roomTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoomType);
    }

    @PutMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> updateRoomType(
            @PathVariable Long roomTypeId,
            @Valid @RequestBody RoomTypeUpdateRequest request) {
        RoomTypeResponse updatedRoomType = roomTypeService.update(roomTypeId, request);
        return ResponseEntity.ok(updatedRoomType);
    }

    @PatchMapping("/{roomTypeId}/status")
    public ResponseEntity<RoomTypeResponse> updateRoomTypeStatus(
            @PathVariable Long roomTypeId,
            @Valid @RequestBody RoomTypeStatusUpdateRequest request) {
        RoomTypeResponse updatedRoomType = roomTypeService.updateStatus(roomTypeId, request);
        return ResponseEntity.ok(updatedRoomType);
    }

    @DeleteMapping("/{roomTypeId}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long roomTypeId) {
        roomTypeService.delete(roomTypeId);
        return ResponseEntity.noContent().build();
    }
}
