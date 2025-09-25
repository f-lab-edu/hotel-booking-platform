package dev.muho.hotel.service;

import dev.muho.hotel.domain.BaseRate;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.dto.request.BaseRateBulkUpdateRequest;
import dev.muho.hotel.dto.request.BaseRateSearchRequest;
import dev.muho.hotel.dto.response.BaseRateResponse;
import dev.muho.hotel.global.exception.RatePlanNotFoundException;
import dev.muho.hotel.repository.BaseRateRepository;
import dev.muho.hotel.repository.RatePlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaseRateService {

    private final RatePlanRepository ratePlanRepository;
    private final BaseRateRepository baseRateRepository;

    public List<BaseRateResponse> getBaseRates(Long ratePlanId, BaseRateSearchRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(RatePlanNotFoundException::new);

        List<LocalDate> allDatesInRange = request.getStartDate().datesUntil(request.getEndDate().plusDays(1))
                .collect(Collectors.toList());

        List<BaseRate> existingBaseRates = baseRateRepository.findByRatePlanAndDateIn(ratePlan, allDatesInRange);

        Map<LocalDate, BaseRate> baseRateMap = existingBaseRates
                .stream()
                .collect(Collectors.toMap(BaseRate::getDate, Function.identity()));

        return allDatesInRange.stream()
                .map(date -> {
                    BaseRate baseRate = baseRateMap.get(date);
                    if (baseRate != null) {
                        return BaseRateResponse.from(baseRate);
                    } else {
                        return BaseRateResponse.builder()
                                .date(date)
                                .price(null)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BaseRateResponse> bulkUpdate(@Valid BaseRateBulkUpdateRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(request.getRatePlanId())
                .orElseThrow(RatePlanNotFoundException::new);

        List<LocalDate> allDatesInRange = request.getStartDate().datesUntil(request.getEndDate().plusDays(1))
                .collect(Collectors.toList());

        List<BaseRate> existingBaseRates = baseRateRepository.findByRatePlanAndDateIn(ratePlan, allDatesInRange);

        Map<LocalDate, BaseRate> existingBaseRatesMap = existingBaseRates
                .stream()
                .collect(Collectors.toMap(BaseRate::getDate, Function.identity()));

        List<BaseRateResponse> updatedBaseRates = new ArrayList<>();

        for (LocalDate date : allDatesInRange) {
            BaseRate baseRate = existingBaseRatesMap.get(date);

            if (baseRate != null) {
                baseRate.updatePrice(request.getPrice());
                updatedBaseRates.add(BaseRateResponse.from(baseRate));
            } else {
                BaseRate newBaseRate = BaseRate.builder()
                        .ratePlan(ratePlan)
                        .date(date)
                        .price(request.getPrice())
                        .build();
                baseRateRepository.save(newBaseRate);
                updatedBaseRates.add(BaseRateResponse.from(newBaseRate));
            }
        }

        return updatedBaseRates;
    }
}
