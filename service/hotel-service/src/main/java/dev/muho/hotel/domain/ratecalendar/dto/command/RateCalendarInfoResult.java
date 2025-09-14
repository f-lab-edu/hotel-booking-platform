package dev.muho.hotel.domain.ratecalendar.dto.command;

import dev.muho.hotel.domain.ratecalendar.entity.AdjustmentType;
import dev.muho.hotel.domain.ratecalendar.entity.RateCalendar;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record RateCalendarInfoResult(
        Long id,
        Long hotelId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Set<DayOfWeek> applicableDays,
        AdjustmentType adjustmentType,
        BigDecimal adjustmentValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RateCalendarInfoResult from(RateCalendar r) {
        return new RateCalendarInfoResult(
                r.getId(),
                r.getHotelId(),
                r.getName(),
                r.getDescription(),
                r.getStartDate(),
                r.getEndDate(),
                r.getApplicableDays(),
                r.getAdjustmentType(),
                r.getAdjustmentValue(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

