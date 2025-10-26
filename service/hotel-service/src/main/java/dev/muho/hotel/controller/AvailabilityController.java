package dev.muho.hotel.controller;

import dev.muho.hotel.dto.request.AvailabilityRequest;
import dev.muho.hotel.dto.response.AvailabilityResponse;
import dev.muho.hotel.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 객실 이용 가능 여부(Availability) 조회를 위한 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    private final Executor asyncTaskExecutor;

    @GetMapping("/api/v1/hotels/{hotelId}/availability")
    public CompletableFuture<ResponseEntity<AvailabilityResponse>> getAvailability(
            @PathVariable Long hotelId,
            @Valid @ModelAttribute AvailabilityRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            AvailabilityResponse response = availabilityService.checkAvailability(hotelId, request);
            return ResponseEntity.ok(response);
        }, asyncTaskExecutor);
    }
}
