package dev.muho.hotellegacy.service;

import dev.muho.hotellegacy.dto.command.RoomInventoryBulkUpdateCommand;
import dev.muho.hotellegacy.dto.command.RoomInventoryInfoResult;
import dev.muho.hotellegacy.dto.command.RoomInventoryUpdateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RoomInventoryService {

    Page<RoomInventoryInfoResult> search(Long roomTypeId, Pageable pageable);

    void bulkUpdate(Long roomTypeId, RoomInventoryBulkUpdateCommand command);

    void update(Long roomTypeId, LocalDate date, RoomInventoryUpdateCommand command);
}

