package dev.muho.hotel.repository;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotel(Hotel hotel);
}
