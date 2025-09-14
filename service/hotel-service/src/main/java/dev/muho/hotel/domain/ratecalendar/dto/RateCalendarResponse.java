package dev.muho.hotel.domain.ratecalendar.dto;

import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarInfoResult;
import dev.muho.hotel.domain.ratecalendar.entity.AdjustmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public class RateCalendarResponse {

    private Long id;
    private Long hotelId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<DayOfWeek> applicableDays;
    private AdjustmentType adjustmentType;
    private BigDecimal adjustmentValue;

    public static RateCalendarResponse from(RateCalendarInfoResult r) {
        return new RateCalendarResponse(
                r.id(),
                r.hotelId(),
                r.name(),
                r.description(),
                r.startDate(),
                r.endDate(),
                r.applicableDays(),
                r.adjustmentType(),
                r.adjustmentValue()
        );
    }
}
