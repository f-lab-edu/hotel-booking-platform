package dev.muho.hotel.domain.ratecalendar;

import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarCreateRequest;
import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarResponse;
import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarUpdateRequest;
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
@RequestMapping("/v1/hotels/{hotelId}/rate-calendars")
@RequiredArgsConstructor
public class RateCalendarController {

    private final FakeRateCalendarRepository rateCalendarRepository;

    @GetMapping
    public Page<RateCalendarResponse> getRateCalendars(@PathVariable Long hotelId, Pageable pageable) {
        return rateCalendarRepository.findAll(hotelId, pageable);
    }

    @GetMapping("/{rateCalendarId}")
    public ResponseEntity<RateCalendarResponse> getRateCalendar(@PathVariable Long hotelId, @PathVariable Long rateCalendarId) {
        RateCalendarResponse rateCalendar = rateCalendarRepository.findById(rateCalendarId);
        if (rateCalendar == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rateCalendar);
    }

    @PostMapping
    public ResponseEntity<RateCalendarResponse> createRateCalendar(@PathVariable Long hotelId, @Valid @RequestBody RateCalendarCreateRequest rateCalendarCreateRequest) {
        RateCalendarResponse createdRateCalendar = rateCalendarRepository.save(hotelId, rateCalendarCreateRequest);
        return ResponseEntity.status(201).body(createdRateCalendar);
    }

    @PutMapping("/{rateCalendarId}")
    public ResponseEntity<RateCalendarResponse> updateRateCalendar(@PathVariable Long hotelId, @PathVariable Long rateCalendarId, @Valid @RequestBody RateCalendarUpdateRequest rateCalendarUpdateRequest) {
        RateCalendarResponse rateCalendar = rateCalendarRepository.findById(rateCalendarId);
        if (rateCalendar == null) {
            return ResponseEntity.notFound().build();
        }
        rateCalendar = rateCalendarRepository.update(hotelId, rateCalendarId, rateCalendarUpdateRequest);
        return ResponseEntity.ok(rateCalendar);
    }

    @DeleteMapping("/{rateCalendarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRateCalendar(@PathVariable Long rateCalendarId) {
        rateCalendarRepository.delete(rateCalendarId);
    }
}
