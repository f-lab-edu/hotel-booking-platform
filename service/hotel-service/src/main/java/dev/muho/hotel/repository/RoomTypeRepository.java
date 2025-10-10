package dev.muho.hotel.repository;

import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    Page<RoomType> findByHotelId(Long hotelId, Pageable pageable);

    // 경우의 수별 검색 메서드들
    Page<RoomType> findByHotelIdAndNameContainingIgnoreCaseAndStatus(
            Long hotelId, String name, Status status, Pageable pageable);

    Page<RoomType> findByHotelIdAndNameContainingIgnoreCase(
            Long hotelId, String name, Pageable pageable);

    Page<RoomType> findByHotelIdAndStatus(
            Long hotelId, Status status, Pageable pageable);
}
