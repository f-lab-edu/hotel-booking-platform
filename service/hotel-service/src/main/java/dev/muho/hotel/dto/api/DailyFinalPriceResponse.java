package dev.muho.hotel.dto.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyFinalPriceResponse {

    private LocalDate date;

    private BigDecimal finalPrice;
}
