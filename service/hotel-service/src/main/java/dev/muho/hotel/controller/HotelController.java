package dev.muho.hotel.controller;

import dev.muho.hotel.dto.request.HotelCreateRequest;
import dev.muho.hotel.dto.request.HotelStatusUpdateRequest;
import dev.muho.hotel.dto.request.HotelUpdateRequest;
import dev.muho.hotel.dto.response.HotelResponse;
import dev.muho.hotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long hotelId) {
        HotelResponse response = hotelService.findHotelById(hotelId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        List<HotelResponse> response = hotelService.findAllHotels();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody HotelCreateRequest request) {
        HotelResponse response = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Long hotelId,
            @Valid @RequestBody HotelUpdateRequest request) {
        HotelResponse response = hotelService.updateHotel(hotelId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long hotelId) {
        hotelService.deleteHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/status")
    public ResponseEntity<HotelResponse> updateHotelStatus(
            @PathVariable Long hotelId,
            @Valid @RequestBody HotelStatusUpdateRequest request) {
        HotelResponse response = hotelService.updateHotelStatus(hotelId, request);
        return ResponseEntity.ok(response);
    }
}
