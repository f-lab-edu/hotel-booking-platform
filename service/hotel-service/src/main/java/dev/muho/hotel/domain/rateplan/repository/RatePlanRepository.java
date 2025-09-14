package dev.muho.hotel.domain.rateplan.repository;

import dev.muho.hotel.domain.rateplan.entity.RatePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatePlanRepository {
    Optional<RatePlan> findById(Long id);
    Page<RatePlan> findByRoomTypeId(Long roomTypeId, Pageable pageable);
    RatePlan save(RatePlan ratePlan);
    RatePlan update(Long id, RatePlan ratePlan);
    void deleteById(Long id);
}

