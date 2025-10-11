package dev.muho.hotel.global.validation;

import java.time.LocalDate;

public interface DateRangeValidatable {
    LocalDate getStartDate();
    LocalDate getEndDate();
}
