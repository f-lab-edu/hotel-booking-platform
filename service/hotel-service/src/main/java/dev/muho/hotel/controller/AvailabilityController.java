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

/**
 * 객실 이용 가능 여부(Availability) 조회를 위한 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/api/v1/hotels/{hotelId}/availability")
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @PathVariable Long hotelId,
            @Valid @ModelAttribute AvailabilityRequest request) {

        // 서비스 계층을 호출하여 비즈니스 로직 수행
        AvailabilityResponse response = availabilityService.checkAvailability(hotelId, request);

        // 성공 응답(200 OK)과 함께 결과 반환
        return ResponseEntity.ok(response);
    }
}
