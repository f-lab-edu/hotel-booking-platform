package dev.muho.hotel.repository;

import dev.muho.hotel.domain.RoomInventory;
import dev.muho.hotel.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {
    /**
     * 특정 객실 타입에 대해, 주어진 날짜 목록에 해당하는 모든 재고 정보를 조회합니다.
     */
    List<RoomInventory> findByRoomTypeAndDateIn(RoomType roomType, List<LocalDate> dates);

    /**
     * 특정 객실 타입과 날짜에 해당하는 재고 정보를 조회합니다.
     */
    Optional<RoomInventory> findByRoomTypeAndDate(RoomType roomType, LocalDate date);
}
