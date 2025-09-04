package dev.muho.hotel.domain.ratecalendar;

import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/v1/hotels/{hotelId}/rate-calendars")
public class RateCalendarController {

    @GetMapping
    public List<RateCalendarResponse> getRateCalendars(@PathVariable Long hotelId) {
        return List.of(
            new RateCalendarResponse(1L, hotelId, "Weekend Discount", "10% off on weekends", LocalDate.of(2025, 10, 11), LocalDate.of(2025, 10, 12), Set.of(DayOfWeek.MONDAY), AdjustmentType.PERCENTAGE, BigDecimal.TEN),
            new RateCalendarResponse(2L, hotelId, "Holiday Surcharge", "20% increase during holidays", LocalDate.of(2025, 11, 11), LocalDate.of(2025, 11, 13), null, AdjustmentType.FIXED_AMOUNT, BigDecimal.valueOf(20))
        );
    }

    @GetMapping("/{rateCalendarId}")
    public RateCalendarResponse getRateCalendar(@PathVariable Long hotelId, @PathVariable Long rateCalendarId) {
        return new RateCalendarResponse(rateCalendarId, hotelId, "Weekend Discount", "10% off on weekends", LocalDate.of(2025, 10, 11), LocalDate.of(2025, 10, 21), Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), AdjustmentType.PERCENTAGE, BigDecimal.TEN);
    }

    @PostMapping
    public RateCalendarResponse createRateCalendar(@PathVariable Long hotelId) {
        return new RateCalendarResponse(3L, hotelId, "New Year Special", "15% off for New Year", LocalDate.of(2025, 10, 11), LocalDate.of(2025, 10, 15), Set.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), AdjustmentType.PERCENTAGE, BigDecimal.TEN);
    }
}
