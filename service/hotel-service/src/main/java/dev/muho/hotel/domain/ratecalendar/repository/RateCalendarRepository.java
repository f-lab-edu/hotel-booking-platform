package dev.muho.hotel.domain.ratecalendar.repository;

import dev.muho.hotel.domain.ratecalendar.entity.RateCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateCalendarRepository {
    Optional<RateCalendar> findById(Long id);
    Page<RateCalendar> searchByHotelId(Long hotelId, Pageable pageable);
    RateCalendar save(RateCalendar rateCalendar);
    RateCalendar update(Long id, RateCalendar rateCalendar);
    void deleteById(Long id);
}

