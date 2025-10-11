package dev.muho.hotel.service;

import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RatePlanCreateRequest;
import dev.muho.hotel.dto.request.RatePlanSearchRequest;
import dev.muho.hotel.dto.request.RatePlanStatusUpdateRequest;
import dev.muho.hotel.dto.request.RatePlanUpdateRequest;
import dev.muho.hotel.dto.response.RatePlanResponse;
import dev.muho.hotel.global.exception.RatePlanNotFoundException;
import dev.muho.hotel.global.exception.RoomTypeNotFoundException;
import dev.muho.hotel.repository.RatePlanRepository;
import dev.muho.hotel.repository.RoomTypeRepository;
import dev.muho.hotel.repository.specification.RatePlanSpecification;
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
public class RatePlanService {

    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;

    public Page<RatePlanResponse> searchRatePlans(@Valid RatePlanSearchRequest request, Pageable pageable) {
        Specification<RatePlan> specification = RatePlanSpecification.searchWith(request);
        return ratePlanRepository.findAll(specification, pageable)
                .map(RatePlanResponse::from);
    }

    public RatePlanResponse findById(Long ratePlanId) {
        RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(RatePlanNotFoundException::new);

        return RatePlanResponse.from(ratePlan);
    }

    @Transactional
    public RatePlanResponse create(@Valid RatePlanCreateRequest request) {
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(RoomTypeNotFoundException::new);

        RatePlan ratePlan = RatePlan.builder()
                .roomType(roomType)
                .name(request.getName())
                .includesBreakfast(request.getIncludesBreakfast())
                .refundable(request.getRefundable())
                .minNights(request.getMinNights())
                .maxNights(request.getMaxNights())
                .bookingStartDate(request.getBookingStartDate())
                .bookingEndDate(request.getBookingEndDate())
                .checkInStartDate(request.getCheckInStartDate())
                .checkInEndDate(request.getCheckInEndDate())
                .build();

        RatePlan savedRatePlan = ratePlanRepository.save(ratePlan);

        return RatePlanResponse.from(savedRatePlan);
    }

    @Transactional
    public RatePlanResponse update(Long ratePlanId, @Valid RatePlanUpdateRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(RatePlanNotFoundException::new);

        ratePlan.update(
                request.getName(),
                request.getIncludesBreakfast(),
                request.getRefundable(),
                request.getMinNights(),
                request.getMaxNights(),
                request.getStatus(),
                request.getBookingStartDate(),
                request.getBookingEndDate(),
                request.getCheckInStartDate(),
                request.getCheckInEndDate()
        );

        return RatePlanResponse.from(ratePlan);
    }

    @Transactional
    public RatePlanResponse updateStatus(Long ratePlanId, @Valid RatePlanStatusUpdateRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(RatePlanNotFoundException::new);

        ratePlan.updateStatus(request.getStatus());

        return RatePlanResponse.from(ratePlan);
    }

    @Transactional
    public void delete(Long ratePlanId) {
        RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(RatePlanNotFoundException::new);

        ratePlan.updateStatus(Status.INACTIVE);
    }
}
