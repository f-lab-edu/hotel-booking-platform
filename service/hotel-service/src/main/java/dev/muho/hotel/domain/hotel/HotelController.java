package dev.muho.hotel.domain.hotel;

import dev.muho.hotel.domain.hotel.dto.HotelCreateRequest;
import dev.muho.hotel.domain.hotel.dto.HotelResponse;
import dev.muho.hotel.domain.hotel.dto.HotelUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final FakeHotelRepository hotelRepository;

    @GetMapping
    public Page<HotelResponse> getHotels(@RequestParam(required = false) String name, Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotel(@PathVariable Long hotelId) {
        HotelResponse hotel = hotelRepository.findById(hotelId);
        if (hotel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hotel);
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody HotelCreateRequest hotelCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelRepository.save(hotelCreateRequest));
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long hotelId, @Valid @RequestBody HotelUpdateRequest hotelUpdateRequest) {
        HotelResponse hotel = hotelRepository.findById(hotelId);
        if (hotel == null) {
            return ResponseEntity.notFound().build();
        }
        hotel = hotelRepository.update(hotelId, hotelUpdateRequest);
        return ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable Long hotelId) {
        hotelRepository.deleteById(hotelId);
    }
}
