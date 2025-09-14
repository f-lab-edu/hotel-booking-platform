package dev.muho.hotel.domain.roominventory.service;

import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryBulkUpdateCommand;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryInfoResult;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryUpdateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RoomInventoryService {

    Page<RoomInventoryInfoResult> search(Long roomTypeId, Pageable pageable);

    void bulkUpdate(Long roomTypeId, RoomInventoryBulkUpdateCommand command);

    void update(Long roomTypeId, LocalDate date, RoomInventoryUpdateCommand command);
}

