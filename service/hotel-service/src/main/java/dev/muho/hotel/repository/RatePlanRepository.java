package dev.muho.hotel.repository;

import dev.muho.hotel.domain.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {
}
