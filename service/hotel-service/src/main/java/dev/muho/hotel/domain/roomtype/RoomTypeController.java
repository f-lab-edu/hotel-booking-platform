package dev.muho.hotel.domain.roomtype;

import dev.muho.hotel.domain.roomtype.dto.RoomTypeCreateRequest;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeResponse;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/hotels/{hotelId}/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final FakeRoomTypeRepository roomTypeRepository;

    @GetMapping
    public Page<RoomTypeResponse> getRoomTypes(@PathVariable Long hotelId, Pageable pageable) {
        return roomTypeRepository.findAll(hotelId, pageable);
    }

    @GetMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> getRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
        RoomTypeResponse roomType = roomTypeRepository.findById(hotelId, roomTypeId);
        if (roomType == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roomType);
    }

    @PostMapping
    public ResponseEntity<RoomTypeResponse> createRoomType(@PathVariable Long hotelId, @Valid @RequestBody RoomTypeCreateRequest roomTypeCreateRequest) {
        RoomTypeResponse createdRoomType = roomTypeRepository.save(hotelId, roomTypeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoomType);
    }

    @PutMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> updateRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId, @Valid @RequestBody RoomTypeUpdateRequest roomTypeUpdateRequest) {
        RoomTypeResponse roomType = roomTypeRepository.findById(hotelId, roomTypeId);
        if (roomType == null) {
            return ResponseEntity.notFound().build();
        }
        roomType = roomTypeRepository.update(hotelId, roomTypeId, roomTypeUpdateRequest);
        return ResponseEntity.ok(roomType);
    }

    @DeleteMapping("/{roomTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
        roomTypeRepository.delete(roomTypeId);
    }
}
