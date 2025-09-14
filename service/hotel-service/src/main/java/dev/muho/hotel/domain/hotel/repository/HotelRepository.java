package dev.muho.hotel.domain.hotel.repository;

import dev.muho.hotel.domain.hotel.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 호텔 영속성 추상화 (구현체는 DB/Redis/InMemory 등).
 * JPA 인터페이스를 사용하지 않고 도메인 포트 형태로 정의.
 */
@Repository
public interface HotelRepository {

    Optional<Hotel> findById(Long id);

    Page<Hotel> search(String name, Pageable pageable);

    Hotel save(Hotel hotel);

    Hotel update(Long id, Hotel hotel);

    void deleteById(Long id);
}

