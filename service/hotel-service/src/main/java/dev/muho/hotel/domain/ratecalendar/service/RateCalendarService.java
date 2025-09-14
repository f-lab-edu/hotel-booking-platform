package dev.muho.hotel.domain.ratecalendar.service;

import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarCreateCommand;
import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarInfoResult;
import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarCreateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RateCalendarService {

    RateCalendarInfoResult create(Long hotelId, RateCalendarCreateCommand command);

    RateCalendarInfoResult findById(Long hotelId, Long id);

    RateCalendarInfoResult update(Long hotelId, Long id, RateCalendarCreateCommand command);

    void deleteById(Long hotelId, Long id);

    Page<RateCalendarInfoResult> search(Long hotelId, Pageable pageable);
}

