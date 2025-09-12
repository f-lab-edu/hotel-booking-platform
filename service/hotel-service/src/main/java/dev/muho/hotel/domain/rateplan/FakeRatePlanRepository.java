package dev.muho.hotel.domain.rateplan;

import dev.muho.hotel.domain.rateplan.dto.RatePlanCreateRequest;
import dev.muho.hotel.domain.rateplan.dto.RatePlanResponse;
import dev.muho.hotel.domain.rateplan.dto.RatePlanUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class FakeRatePlanRepository {

    private final Map<Long, RatePlanResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<RatePlanResponse> findAll(Long roomTypeId, Pageable pageable) {
        List<RatePlanResponse> sortedRoomTypes = new ArrayList<>(db.values())
                .stream()
                .filter(ratePlanResponse -> ratePlanResponse.getRoomTypeId() == roomTypeId)
                .collect(Collectors.toList());

        sortedRoomTypes.sort(Comparator.comparing(RatePlanResponse::getId));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedRoomTypes.size());

        if (start > sortedRoomTypes.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, sortedRoomTypes.size());
        }

        List<RatePlanResponse> content = sortedRoomTypes.subList(start, end);

        return new PageImpl<>(content, pageable, sortedRoomTypes.size());
    }

    public RatePlanResponse findById(Long id) {
        return db.get(id);
    }

    public RatePlanResponse save(Long roomTypeId, RatePlanCreateRequest request) {
        RatePlanResponse newRatePlan = new RatePlanResponse(currentId++, roomTypeId, request.getName(), request.getDescription(), request.getBasePrice(), request.isBreakfastIncluded(), request.isRefundable(), request.getMinNights(), request.getMaxNights());
        db.put(newRatePlan.getId(), newRatePlan);
        return newRatePlan;
    }

    public RatePlanResponse update(Long roomTypeId, Long ratePlanId, RatePlanUpdateRequest request) {
        RatePlanResponse updatedRatePlan = new RatePlanResponse(ratePlanId, roomTypeId, request.getName(), request.getDescription(), request.getBasePrice(), request.isBreakfastIncluded(), request.isRefundable(), request.getMinNights(), request.getMaxNights());
        db.put(ratePlanId, updatedRatePlan);
        return updatedRatePlan;
    }

    public void delete(Long ratePlanId) {
        db.remove(ratePlanId);
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}
