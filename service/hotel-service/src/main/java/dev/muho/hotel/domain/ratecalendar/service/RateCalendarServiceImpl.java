package dev.muho.hotel.domain.ratecalendar.service;

import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarCreateCommand;
import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarInfoResult;
import dev.muho.hotel.domain.ratecalendar.entity.RateCalendar;
import dev.muho.hotel.domain.ratecalendar.error.RateCalendarNotFoundException;
import dev.muho.hotel.domain.ratecalendar.repository.RateCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RateCalendarServiceImpl implements RateCalendarService {

    private final RateCalendarRepository repository;

    @Override
    @Transactional
    public RateCalendarInfoResult create(Long hotelId, RateCalendarCreateCommand command) {
        RateCalendar r = RateCalendar.createNew(hotelId, command.name(), command.description(), command.startDate(), command.endDate(), command.applicableDays(), command.adjustmentType(), command.adjustmentValue());
        RateCalendar saved = repository.save(r);
        return RateCalendarInfoResult.from(saved);
    }

    @Override
    public RateCalendarInfoResult findById(Long hotelId, Long id) {
        RateCalendar r = repository.findById(id).orElseThrow(RateCalendarNotFoundException::new);
        if (!r.getHotelId().equals(hotelId)) throw new RateCalendarNotFoundException();
        return RateCalendarInfoResult.from(r);
    }

    @Override
    @Transactional
    public RateCalendarInfoResult update(Long hotelId, Long id, RateCalendarCreateCommand command) {
        RateCalendar updating = RateCalendar.builder()
                .id(id)
                .hotelId(hotelId)
                .name(command.name())
                .description(command.description())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .applicableDays(command.applicableDays())
                .adjustmentType(command.adjustmentType())
                .adjustmentValue(command.adjustmentValue())
                .build();
        RateCalendar updated = repository.update(id, updating);
        return RateCalendarInfoResult.from(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long hotelId, Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<RateCalendarInfoResult> search(Long hotelId, Pageable pageable) {
        return repository.searchByHotelId(hotelId, pageable).map(RateCalendarInfoResult::from);
    }
}

