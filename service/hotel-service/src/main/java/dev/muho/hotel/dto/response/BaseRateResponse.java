package dev.muho.hotel.dto.response;

import dev.muho.hotel.domain.BaseRate;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class BaseRateResponse {

    private LocalDate date;
    private BigDecimal price;

    public static BaseRateResponse from(BaseRate baseRate) {
        return BaseRateResponse.builder()
                .date(baseRate.getDate())
                .price(baseRate.getPrice())
                .build();
    }
}
