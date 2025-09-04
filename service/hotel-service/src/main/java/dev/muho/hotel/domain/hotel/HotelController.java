package dev.muho.hotel.domain.hotel;

import dev.muho.hotel.domain.hotel.dto.HotelCreateRequest;
import dev.muho.hotel.domain.hotel.dto.HotelResponse;
import dev.muho.hotel.domain.hotel.dto.HotelUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/v1/hotels")
public class HotelController {

    @GetMapping
    public List<HotelResponse> getHotels(@RequestParam(required = false) String name, @RequestParam int page, @RequestParam int size) {
        return List.of(
            new HotelResponse(1L, "Hotel California", "42 Sunset Blvd", "USA", "Los Angeles", 5, "A lovely place", "+1-234-567-8900"),
            new HotelResponse(2L, "The Grand Budapest", "1 Alpine St", "Hungary", "Budapest", 4, "A grand hotel", "+36-1-234-5678")
        );
    }

    @GetMapping("/{hotelId}")
    public HotelResponse getHotel(@PathVariable Long hotelId) {
        return new HotelResponse(hotelId, "Hotel California", "42 Sunset Blvd", "USA", "Los Angeles", 5, "A lovely place", "+1-234-567-8900");
    }

    @PostMapping
    public HotelResponse createHotel(@Valid @RequestBody HotelCreateRequest hotelCreateRequest) {
        return new HotelResponse(1L, hotelCreateRequest.getName(), hotelCreateRequest.getAddress(), hotelCreateRequest.getCountry(), hotelCreateRequest.getCity(), hotelCreateRequest.getRating(), hotelCreateRequest.getDescription(), hotelCreateRequest.getContactNumber());
    }

    @PutMapping("/{hotelId}")
    public HotelResponse updateHotel(@PathVariable Long hotelId, @Valid @RequestBody HotelUpdateRequest hotelUpdateRequest) {
        return new HotelResponse(hotelId, hotelUpdateRequest.getName(), hotelUpdateRequest.getAddress(), hotelUpdateRequest.getCountry(), hotelUpdateRequest.getCity(), hotelUpdateRequest.getRating(), hotelUpdateRequest.getDescription(), hotelUpdateRequest.getContactNumber());
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable Long hotelId) {
    }
}
