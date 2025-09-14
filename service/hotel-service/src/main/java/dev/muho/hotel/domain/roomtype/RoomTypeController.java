package dev.muho.hotel.domain.roomtype;

import dev.muho.hotel.domain.roomtype.dto.RoomTypeCreateRequest;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeResponse;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeUpdateRequest;
import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeCreateCommand;
import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeInfoResult;
import dev.muho.hotel.domain.roomtype.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/hotels/{hotelId}/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public Page<RoomTypeResponse> getRoomTypes(@PathVariable Long hotelId, Pageable pageable) {
        return roomTypeService.search(hotelId, pageable).map(RoomTypeResponse::from);
    }

    @GetMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> getRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
        RoomTypeInfoResult r = roomTypeService.findById(hotelId, roomTypeId);
        return ResponseEntity.ok(RoomTypeResponse.from(r));
    }

    @PostMapping
    public ResponseEntity<RoomTypeResponse> createRoomType(@PathVariable Long hotelId, @Valid @RequestBody RoomTypeCreateRequest roomTypeCreateRequest) {
        RoomTypeCreateCommand cmd = new RoomTypeCreateCommand(
                roomTypeCreateRequest.getName(),
                roomTypeCreateRequest.getDescription(),
                roomTypeCreateRequest.getMaxOccupancy(),
                roomTypeCreateRequest.getStandardOccupancy(),
                roomTypeCreateRequest.getViewType(),
                roomTypeCreateRequest.getBedType()
        );
        RoomTypeInfoResult created = roomTypeService.create(hotelId, cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomTypeResponse.from(created));
    }

    @PutMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> updateRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId, @Valid @RequestBody RoomTypeUpdateRequest roomTypeUpdateRequest) {
        RoomTypeCreateCommand cmd = new RoomTypeCreateCommand(
                roomTypeUpdateRequest.getName(),
                roomTypeUpdateRequest.getDescription(),
                roomTypeUpdateRequest.getMaxOccupancy(),
                roomTypeUpdateRequest.getStandardOccupancy(),
                roomTypeUpdateRequest.getViewType(),
                roomTypeUpdateRequest.getBedType()
        );
        RoomTypeInfoResult updated = roomTypeService.update(hotelId, roomTypeId, cmd);
        return ResponseEntity.ok(RoomTypeResponse.from(updated));
    }

    @DeleteMapping("/{roomTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
        roomTypeService.deleteById(hotelId, roomTypeId);
    }
}
