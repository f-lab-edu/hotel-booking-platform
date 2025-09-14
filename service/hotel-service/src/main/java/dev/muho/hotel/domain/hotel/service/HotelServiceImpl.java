package dev.muho.hotel.domain.hotel.service;

import dev.muho.hotel.domain.hotel.dto.command.HotelCreateCommand;
import dev.muho.hotel.domain.hotel.dto.command.HotelInfoResult;
import dev.muho.hotel.domain.hotel.dto.command.HotelSearchCondition;
import dev.muho.hotel.domain.hotel.dto.command.HotelUpdateCommand;
import dev.muho.hotel.domain.hotel.entity.Hotel;
import dev.muho.hotel.domain.hotel.error.HotelNotFoundException;
import dev.muho.hotel.domain.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public HotelInfoResult create(HotelCreateCommand command) {
        Hotel hotel = Hotel.createNew(
                command.name(),
                command.address(),
                command.country(),
                command.city(),
                command.rating(),
                command.description(),
                command.contactNumber()
        );
        Hotel saved = hotelRepository.save(hotel);
        return HotelInfoResult.from(saved);
    }

    @Override
    public HotelInfoResult findById(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow(HotelNotFoundException::new);
        return HotelInfoResult.from(hotel);
    }

    @Override
    @Transactional
    public HotelInfoResult update(Long id, HotelUpdateCommand command) {
        // create an updated hotel object or use repository.update
        Hotel updating = Hotel.builder()
                .id(id)
                .name(command.name())
                .address(command.address())
                .country(command.country())
                .city(command.city())
                .rating(command.rating())
                .description(command.description())
                .contactNumber(command.contactNumber())
                .build();

        Hotel updated = hotelRepository.update(id, updating);
        return HotelInfoResult.from(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        hotelRepository.deleteById(id);
    }

    @Override
    public Page<HotelInfoResult> search(HotelSearchCondition condition, Pageable pageable) {
        return hotelRepository.search(condition.name(), pageable).map(HotelInfoResult::from);
    }
}

