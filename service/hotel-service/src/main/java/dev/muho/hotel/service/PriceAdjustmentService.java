package dev.muho.hotel.service;

import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.dto.request.PriceAdjustmentCreateRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentSearchRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentUpdateRequest;
import dev.muho.hotel.dto.response.PriceAdjustmentResponse;
import dev.muho.hotel.global.exception.PriceAdjustmentNotFoundException;
import dev.muho.hotel.global.exception.RatePlanNotFoundException;
import dev.muho.hotel.repository.PriceAdjustmentRepository;
import dev.muho.hotel.repository.RatePlanRepository;
import dev.muho.hotel.repository.specification.PriceAdjustmentSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceAdjustmentService {

    private final RatePlanRepository ratePlanRepository;
    private final PriceAdjustmentRepository priceAdjustmentRepository;


    public Page<PriceAdjustmentResponse> search(@Valid PriceAdjustmentSearchRequest request, Pageable pageable) {
        Specification<PriceAdjustment> specification = PriceAdjustmentSpecification.searchWith(request);
        return priceAdjustmentRepository.findAll(specification, pageable)
                .map(PriceAdjustmentResponse::from);
    }

    public PriceAdjustmentResponse findById(Long adjustmentId) {
        PriceAdjustment priceAdjustment = priceAdjustmentRepository.findById(adjustmentId)
                .orElseThrow(PriceAdjustmentNotFoundException::new);

        return PriceAdjustmentResponse.from(priceAdjustment);
    }

    @Transactional
    public PriceAdjustmentResponse create(@Valid PriceAdjustmentCreateRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(request.getRatePlanId())
                .orElseThrow(RatePlanNotFoundException::new);

        PriceAdjustment priceAdjustment = PriceAdjustment.builder()
                .ratePlan(ratePlan)
                .name(request.getName())
                .adjustmentType(request.getAdjustmentType())
                .calculationType(request.getCalculationType())
                .amount(request.getAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .bookingDaysBeforeArrival(request.getBookingDaysBeforeArrival())
                .build();

        PriceAdjustment savedAdjustment = priceAdjustmentRepository.save(priceAdjustment);
        return PriceAdjustmentResponse.from(savedAdjustment);
    }

    @Transactional
    public PriceAdjustmentResponse update(Long adjustmentId, @Valid PriceAdjustmentUpdateRequest request) {
        PriceAdjustment priceAdjustment = priceAdjustmentRepository.findById(adjustmentId)
                .orElseThrow(PriceAdjustmentNotFoundException::new);

        priceAdjustment.update(
                request.getName(),
                request.getAdjustmentType(),
                request.getCalculationType(),
                request.getAmount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getBookingDaysBeforeArrival()
        );

        return PriceAdjustmentResponse.from(priceAdjustment);
    }
}
