package dev.muho.hotel.domain.rateplan;

import dev.muho.hotel.domain.rateplan.dto.DailyFinalPriceResponse;
import dev.muho.hotel.domain.rateplan.dto.FinalPriceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/rate-plans/{ratePlanId}/final-price")
public class FinalPriceController {

    @GetMapping
    public List<DailyFinalPriceResponse> getFinalPrices(@PathVariable Long ratePlanId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return List.of(
            new DailyFinalPriceResponse(LocalDate.of(2025, 10, 11), BigDecimal.TEN),
            new DailyFinalPriceResponse(LocalDate.of(2025, 10, 12), BigDecimal.valueOf(20))
        );
    }

    @GetMapping("/{date}")
    public FinalPriceResponse getFinalPriceByDate(@PathVariable Long ratePlanId, @PathVariable LocalDate date) {
        return new FinalPriceResponse(date, BigDecimal.TEN, BigDecimal.valueOf(15), List.of("주말", "성수기"), List.of("조기 예약 할인"));
    }
}
