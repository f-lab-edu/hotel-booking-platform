package dev.muho.hotel.domain.roominventory.service;

import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryBulkUpdateCommand;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryInfoResult;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryUpdateCommand;
import dev.muho.hotel.domain.roominventory.entity.RoomInventory;
import dev.muho.hotel.domain.roominventory.repository.RoomInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomInventoryServiceImpl implements RoomInventoryService {

    private final RoomInventoryRepository repository;

    @Override
    public Page<RoomInventoryInfoResult> search(Long roomTypeId, Pageable pageable) {
        return repository.findByRoomTypeId(roomTypeId, pageable).map(RoomInventoryInfoResult::from);
    }

    @Override
    @Transactional
    public void bulkUpdate(Long roomTypeId, RoomInventoryBulkUpdateCommand command) {
        LocalDate start = command.startDate();
        LocalDate end = command.endDate();
        if (start.isAfter(end)) throw new IllegalArgumentException("startDate must be before or equal to endDate");

        List<RoomInventory> toSave = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            // find existing inventory for date
            repository.findByRoomTypeIdAndDate(roomTypeId, date).ifPresentOrElse(existing -> {
                existing.update(command.totalRooms(), command.availableRooms());
                repository.update(existing.getId(), existing);
            }, () -> {
                RoomInventory created = RoomInventory.of(roomTypeId, date, command.totalRooms(), command.availableRooms());
                repository.save(created);
            });
        }
    }

    @Override
    @Transactional
    public void update(Long roomTypeId, LocalDate date, RoomInventoryUpdateCommand command) {
        repository.findByRoomTypeIdAndDate(roomTypeId, date).ifPresentOrElse(existing -> {
            existing.update(command.totalRooms(), command.availableRooms());
            repository.update(existing.getId(), existing);
        }, () -> {
            RoomInventory created = RoomInventory.of(roomTypeId, date, command.totalRooms(), command.availableRooms());
            repository.save(created);
        });
    }
}

