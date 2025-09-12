package dev.muho.hotel.domain.ratecalendar;

import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarCreateRequest;
import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarResponse;
import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarUpdateRequest;
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
public class FakeRateCalendarRepository {

    private final Map<Long, RateCalendarResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<RateCalendarResponse> findAll(Long hotelId, Pageable pageable) {
        List<RateCalendarResponse> sortedRateCalendars = new ArrayList<>(db.values())
                .stream()
                .filter(rateCalendarResponse -> rateCalendarResponse.getHotelId() == hotelId)
                .collect(Collectors.toList());

        sortedRateCalendars.sort(Comparator.comparing(RateCalendarResponse::getId));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedRateCalendars.size());

        if (start > sortedRateCalendars.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, sortedRateCalendars.size());
        }

        List<RateCalendarResponse> content = sortedRateCalendars.subList(start, end);

        return new PageImpl<>(content, pageable, sortedRateCalendars.size());
    }

    public RateCalendarResponse findById(Long id) {
        return db.get(id);
    }

    public RateCalendarResponse save(Long hotelId, RateCalendarCreateRequest request) {
        RateCalendarResponse rateCalendarResponse = new RateCalendarResponse(currentId++, hotelId, request.getName(), request.getDescription(), request.getStartDate(), request.getEndDate(), request.getApplicableDays(), request.getAdjustmentType(), request.getAdjustmentValue());
        db.put(rateCalendarResponse.getId(), rateCalendarResponse);
        return rateCalendarResponse;
    }

    public RateCalendarResponse update(Long hotelId, Long rateCalendarId, RateCalendarUpdateRequest request) {
        RateCalendarResponse rateCalendarResponse = new RateCalendarResponse(rateCalendarId, hotelId, request.getName(), request.getDescription(), request.getStartDate(), request.getEndDate(), request.getApplicableDays(), request.getAdjustmentType(), request.getAdjustmentValue());
        db.put(rateCalendarId, rateCalendarResponse);
        return rateCalendarResponse;
    }

    public void delete(Long rateCalendarId) {
        db.remove(rateCalendarId);
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}
