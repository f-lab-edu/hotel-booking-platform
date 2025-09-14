package dev.muho.hotel.domain.ratecalendar.dto.command;

import dev.muho.hotel.domain.ratecalendar.entity.AdjustmentType;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public record RateCalendarCreateCommand(
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Set<DayOfWeek> applicableDays,
        AdjustmentType adjustmentType,
        BigDecimal adjustmentValue
) {}

