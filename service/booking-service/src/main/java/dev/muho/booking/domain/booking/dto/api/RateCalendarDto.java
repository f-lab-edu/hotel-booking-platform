package dev.muho.booking.domain.booking.dto.api;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RateCalendarDto {
    private Long rateCalendarId;
    private String rateCalendarName;
    private String adjustmentType;
    private BigDecimal adjustmentValue;
}
