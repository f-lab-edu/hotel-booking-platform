package dev.muho.hotel.domain.hotel.service;

import dev.muho.hotel.domain.hotel.dto.command.HotelCreateCommand;
import dev.muho.hotel.domain.hotel.dto.command.HotelInfoResult;
import dev.muho.hotel.domain.hotel.dto.command.HotelSearchCondition;
import dev.muho.hotel.domain.hotel.dto.command.HotelUpdateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelService {

    HotelInfoResult create(HotelCreateCommand command);

    HotelInfoResult findById(Long id);

    HotelInfoResult update(Long id, HotelUpdateCommand command);

    void deleteById(Long id);

    Page<HotelInfoResult> search(HotelSearchCondition condition, Pageable pageable);
}

