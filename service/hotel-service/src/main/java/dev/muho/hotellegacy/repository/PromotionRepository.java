package dev.muho.hotellegacy.repository;

import dev.muho.hotellegacy.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository {
    Optional<Promotion> findById(Long id);
    Page<Promotion> search(Long hotelId, Pageable pageable);
    Promotion save(Promotion promotion);
    Promotion update(Long id, Promotion promotion);
    void deleteById(Long id);
}

