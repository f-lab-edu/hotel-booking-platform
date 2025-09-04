package dev.muho.hotel.domain.roomtype;

import dev.muho.hotel.domain.roomtype.dto.RoomTypeCreateRequest;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeResponse;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/hotels/{hotelId}/room-types")
public class RoomTypeController {

    @GetMapping
    public List<RoomTypeResponse> getRoomTypes(@PathVariable Long hotelId) {
        return List.of(
                new RoomTypeResponse(1L, "Single", "A single room", 1, 1, "ocean view", "queen bed"),
                new RoomTypeResponse(2L, "Double", "A double room", 2, 2, "city view", "two double beds")
        );
    }

    @GetMapping("/{roomTypeId}")
    public RoomTypeResponse getRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
        return new RoomTypeResponse(roomTypeId, "Single", "A single room", 1, 1, "ocean view", "queen bed");
    }

    @PostMapping
    public RoomTypeResponse createRoomType(@PathVariable Long hotelId, @Valid @RequestBody RoomTypeCreateRequest roomTypeCreateRequest) {
        return new RoomTypeResponse(1L, roomTypeCreateRequest.getName(), roomTypeCreateRequest.getDescription(), roomTypeCreateRequest.getMaxOccupancy(), roomTypeCreateRequest.getStandardOccupancy(), roomTypeCreateRequest.getViewType(), roomTypeCreateRequest.getBedType());
    }

    @PutMapping("/{roomTypeId}")
    public RoomTypeResponse updateRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId, @Valid @RequestBody RoomTypeUpdateRequest roomTypeUpdateRequest) {
        return new RoomTypeResponse(roomTypeId, roomTypeUpdateRequest.getName(), roomTypeUpdateRequest.getDescription(), roomTypeUpdateRequest.getMaxOccupancy(), roomTypeUpdateRequest.getStandardOccupancy(), roomTypeUpdateRequest.getViewType(), roomTypeUpdateRequest.getBedType());
    }

    @DeleteMapping("/{roomTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
    }
}
