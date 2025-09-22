package dev.muho.hotel.controller;

import dev.muho.hotel.dto.api.PromotionCreateRequest;
import dev.muho.hotel.dto.command.RateCalendarCreateCommand;
import dev.muho.hotel.dto.command.RateCalendarInfoResult;
import dev.muho.hotel.service.RateCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/hotels/{hotelId}/rate-calendars")
@RequiredArgsConstructor
public class RateCalendarController {

    private final RateCalendarService service;

    @GetMapping
    public Page<PromotionCreateRequest.RateCalendarResponse> getRateCalendars(@PathVariable Long hotelId, Pageable pageable) {
        return service.search(hotelId, pageable).map(PromotionCreateRequest.RateCalendarResponse::from);
    }

    @GetMapping("/{rateCalendarId}")
    public ResponseEntity<PromotionCreateRequest.RateCalendarResponse> getRateCalendar(@PathVariable Long hotelId, @PathVariable Long rateCalendarId) {
        RateCalendarInfoResult r = service.findById(hotelId, rateCalendarId);
        return ResponseEntity.ok(PromotionCreateRequest.RateCalendarResponse.from(r));
    }

    @PostMapping
    public ResponseEntity<PromotionCreateRequest.RateCalendarResponse> createRateCalendar(@PathVariable Long hotelId, @Valid @RequestBody PromotionCreateRequest.RateCalendarCreateRequest rateCalendarCreateRequest) {
        RateCalendarCreateCommand cmd = new RateCalendarCreateCommand(
                rateCalendarCreateRequest.getName(),
                rateCalendarCreateRequest.getDescription(),
                rateCalendarCreateRequest.getStartDate(),
                rateCalendarCreateRequest.getEndDate(),
                rateCalendarCreateRequest.getApplicableDays(),
                rateCalendarCreateRequest.getAdjustmentType(),
                rateCalendarCreateRequest.getAdjustmentValue()
        );
        RateCalendarInfoResult created = service.create(hotelId, cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(PromotionCreateRequest.RateCalendarResponse.from(created));
    }

    @PutMapping("/{rateCalendarId}")
    public ResponseEntity<PromotionCreateRequest.RateCalendarResponse> updateRateCalendar(@PathVariable Long hotelId, @PathVariable Long rateCalendarId, @Valid @RequestBody PromotionCreateRequest.RateCalendarUpdateRequest rateCalendarUpdateRequest) {
        RateCalendarCreateCommand cmd = new RateCalendarCreateCommand(
                rateCalendarUpdateRequest.getName(),
                rateCalendarUpdateRequest.getDescription(),
                rateCalendarUpdateRequest.getStartDate(),
                rateCalendarUpdateRequest.getEndDate(),
                rateCalendarUpdateRequest.getApplicableDays(),
                rateCalendarUpdateRequest.getAdjustmentType(),
                rateCalendarUpdateRequest.getAdjustmentValue()
        );
        RateCalendarInfoResult updated = service.update(hotelId, rateCalendarId, cmd);
        return ResponseEntity.ok(PromotionCreateRequest.RateCalendarResponse.from(updated));
    }

    @DeleteMapping("/{rateCalendarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRateCalendar(@PathVariable Long hotelId, @PathVariable Long rateCalendarId) {
        service.deleteById(hotelId, rateCalendarId);
    }
}
