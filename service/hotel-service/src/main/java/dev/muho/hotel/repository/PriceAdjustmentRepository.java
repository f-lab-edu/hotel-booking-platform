package dev.muho.hotel.repository;

import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PriceAdjustmentRepository extends JpaRepository<PriceAdjustment, Long>, JpaSpecificationExecutor<PriceAdjustment> {
    @Query("SELECT pa FROM PriceAdjustment pa WHERE pa.ratePlan = :ratePlan AND pa.startDate <= :stayEnd AND pa.endDate >= :stayStart")
    List<PriceAdjustment> findActiveAdjustmentsForPlanInDateRange(RatePlan ratePlan, LocalDate stayStart, LocalDate stayEnd);
}
