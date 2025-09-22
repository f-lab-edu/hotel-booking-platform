package dev.muho.hotellegacy.service;

import dev.muho.hotellegacy.dto.command.HotelCreateCommand;
import dev.muho.hotellegacy.dto.command.HotelInfoResult;
import dev.muho.hotellegacy.dto.command.HotelSearchCondition;
import dev.muho.hotellegacy.dto.command.HotelUpdateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelService {

    HotelInfoResult create(HotelCreateCommand command);

    HotelInfoResult findById(Long id);

    HotelInfoResult update(Long id, HotelUpdateCommand command);

    void deleteById(Long id);

    Page<HotelInfoResult> search(HotelSearchCondition condition, Pageable pageable);
}

