package dev.muho.hotel.domain.roominventory.repository;

import dev.muho.hotel.domain.roominventory.entity.RoomInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RoomInventoryRepository {
    Optional<RoomInventory> findByRoomTypeIdAndDate(Long roomTypeId, LocalDate date);
    Page<RoomInventory> findByRoomTypeId(Long roomTypeId, Pageable pageable);
    RoomInventory save(RoomInventory inventory);
    RoomInventory update(Long id, RoomInventory inventory);
    void deleteById(Long id);
}

