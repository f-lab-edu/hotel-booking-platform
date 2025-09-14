package dev.muho.hotel.domain.hotel;

import dev.muho.hotel.domain.hotel.dto.api.HotelCreateRequest;
import dev.muho.hotel.domain.hotel.dto.api.HotelResponse;
import dev.muho.hotel.domain.hotel.dto.api.HotelUpdateRequest;
import dev.muho.hotel.domain.hotel.dto.command.HotelCreateCommand;
import dev.muho.hotel.domain.hotel.dto.command.HotelSearchCondition;
import dev.muho.hotel.domain.hotel.dto.command.HotelUpdateCommand;
import dev.muho.hotel.domain.hotel.dto.command.HotelInfoResult;
import dev.muho.hotel.domain.hotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public Page<HotelResponse> getHotels(@RequestParam(required = false) String name, Pageable pageable) {
        HotelSearchCondition condition = HotelSearchCondition.of(name);
        return hotelService.search(condition, pageable).map(HotelResponse::from);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotel(@PathVariable Long hotelId) {
        try {
            HotelInfoResult result = hotelService.findById(hotelId);
            return ResponseEntity.ok(HotelResponse.from(result));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody HotelCreateRequest hotelCreateRequest) {
        HotelCreateCommand command = new HotelCreateCommand(
                hotelCreateRequest.getName(),
                hotelCreateRequest.getAddress(),
                hotelCreateRequest.getCountry(),
                hotelCreateRequest.getCity(),
                hotelCreateRequest.getRating(),
                hotelCreateRequest.getDescription(),
                hotelCreateRequest.getContactNumber()
        );
        HotelInfoResult created = hotelService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(HotelResponse.from(created));
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long hotelId, @Valid @RequestBody HotelUpdateRequest hotelUpdateRequest) {
        try {
            HotelUpdateCommand command = new HotelUpdateCommand(
                    hotelUpdateRequest.getName(),
                    hotelUpdateRequest.getAddress(),
                    hotelUpdateRequest.getCountry(),
                    hotelUpdateRequest.getCity(),
                    hotelUpdateRequest.getRating(),
                    hotelUpdateRequest.getDescription(),
                    hotelUpdateRequest.getContactNumber()
            );
            HotelInfoResult updated = hotelService.update(hotelId, command);
            return ResponseEntity.ok(HotelResponse.from(updated));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable Long hotelId) {
        hotelService.deleteById(hotelId);
    }
}
