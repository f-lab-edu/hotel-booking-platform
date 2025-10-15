package dev.muho.hotel.repository;

import dev.muho.hotel.domain.BaseRate;
import dev.muho.hotel.domain.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BaseRateRepository extends JpaRepository<BaseRate, Long> {
    /** 특정 요금제에 대해, 주어진 날짜 목록에 해당하는 모든 기본 요금 정보를 조회합니다. */
    List<BaseRate> findByRatePlanAndDateIn(RatePlan ratePlan, List<LocalDate> dates);

    List<BaseRate> findByRatePlanInAndDateIn(List<RatePlan> ratePlans, List<LocalDate> dates);
}
