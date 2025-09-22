package dev.muho.hotel.repository;

import dev.muho.hotel.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository {
    Optional<RoomType> findById(Long id);
    Page<RoomType> findByHotelId(Long hotelId, Pageable pageable);
    RoomType save(RoomType roomType);
    RoomType update(Long id, RoomType roomType);
    void deleteById(Long id);
}

