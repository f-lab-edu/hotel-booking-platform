package dev.muho.hotel.domain.rateplan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class FinalPriceResponse {

    private LocalDate date;

    private BigDecimal basePrice;

    private BigDecimal finalPrice;

    private List<String> appliedRateCalendars;

    private List<String> appliedPromotions;
}
