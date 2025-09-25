package dev.muho.hotel.controller;

import dev.muho.hotel.dto.request.BaseRateBulkUpdateRequest;
import dev.muho.hotel.dto.request.BaseRateSearchRequest;
import dev.muho.hotel.dto.response.BaseRateResponse;
import dev.muho.hotel.service.BaseRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BaseRateController {

    private final BaseRateService baseRateService;

    @GetMapping("/rate-plans/{ratePlanId}/base-rates")
    public ResponseEntity<List<BaseRateResponse>> getBaseRates(
            @PathVariable Long ratePlanId,
            @Valid @ModelAttribute BaseRateSearchRequest request) {
        List<BaseRateResponse> baseRates = baseRateService.getBaseRates(ratePlanId, request);
        return ResponseEntity.ok(baseRates);
    }

    @PutMapping("/base-rates/bulk-update")
    public ResponseEntity<List<BaseRateResponse>> bulkUpdateBaseRates(@Valid @RequestBody BaseRateBulkUpdateRequest request) {
        List<BaseRateResponse> baseRates = baseRateService.bulkUpdate(request);
        return ResponseEntity.ok(baseRates);
    }
}
