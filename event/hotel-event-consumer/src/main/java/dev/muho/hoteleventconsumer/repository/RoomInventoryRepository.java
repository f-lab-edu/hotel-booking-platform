package dev.muho.hoteleventconsumer.repository;

import dev.muho.hoteleventconsumer.domain.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    List<RoomInventory> findByRoomTypeIdAndDateIn(Long roomTypeId, List<LocalDate> dates);
}
