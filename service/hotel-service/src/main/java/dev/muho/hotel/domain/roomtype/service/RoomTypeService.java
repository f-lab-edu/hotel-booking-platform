package dev.muho.hotel.domain.roomtype.service;

import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeCreateCommand;
import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeInfoResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomTypeService {

    RoomTypeInfoResult create(Long hotelId, RoomTypeCreateCommand command);

    RoomTypeInfoResult findById(Long hotelId, Long id);

    RoomTypeInfoResult update(Long hotelId, Long id, RoomTypeCreateCommand command);

    void deleteById(Long hotelId, Long id);

    Page<RoomTypeInfoResult> search(Long hotelId, Pageable pageable);
}

