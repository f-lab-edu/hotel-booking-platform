package dev.muho.hotel.domain.rateplan;

import dev.muho.hotel.domain.rateplan.dto.RatePlanCreateRequest;
import dev.muho.hotel.domain.rateplan.dto.RatePlanResponse;
import dev.muho.hotel.domain.rateplan.dto.RatePlanUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/room-types/{roomTypeId}/rate-plans")
public class RatePlanController {

    @GetMapping
    public List<RatePlanResponse> getRatePlans(@PathVariable Long roomTypeId) {
        return List.of(
                new RatePlanResponse(1L, "Standard Rate", "Standard rate plan", BigDecimal.valueOf(100), true, true, 1, 30),
                new RatePlanResponse(2L, "Non-Refundable Rate", "Non-refundable rate plan", BigDecimal.valueOf(80), false, false, 1, 30)
        );
    }

    @GetMapping("/{ratePlanId}")
    public RatePlanResponse getRatePlan(@PathVariable Long roomTypeId, @PathVariable Long ratePlanId) {
        return new RatePlanResponse(ratePlanId, "Standard Rate", "Standard rate plan", BigDecimal.valueOf(100), true, true, 1, 30);
    }

    @PostMapping
    public RatePlanResponse createRatePlan(@PathVariable Long roomTypeId, @Valid @RequestBody RatePlanCreateRequest ratePlanCreateRequest) {
        return new RatePlanResponse(1L, ratePlanCreateRequest.getName(), ratePlanCreateRequest.getDescription(), ratePlanCreateRequest.getBasePrice(), ratePlanCreateRequest.isBreakfastIncluded(), ratePlanCreateRequest.isRefundable(), ratePlanCreateRequest.getMinNights(), ratePlanCreateRequest.getMaxNights());
    }

    @PutMapping("/{ratePlanId}")
    public RatePlanResponse updateRatePlan(@PathVariable Long roomTypeId, @PathVariable Long ratePlanId, @Valid @RequestBody RatePlanUpdateRequest ratePlanUpdateRequest) {
        return new RatePlanResponse(ratePlanId, ratePlanUpdateRequest.getName(), ratePlanUpdateRequest.getDescription(), ratePlanUpdateRequest.getBasePrice(), ratePlanUpdateRequest.isBreakfastIncluded(), ratePlanUpdateRequest.isRefundable(), ratePlanUpdateRequest.getMinNights(), ratePlanUpdateRequest.getMaxNights());
    }

    @DeleteMapping("/{ratePlanId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRatePlan(@PathVariable Long roomTypeId, @PathVariable Long ratePlanId) {
    }
}
