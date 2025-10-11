package dev.muho.hotel.repository.specification;

import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.PriceAdjustmentSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PriceAdjustmentSpecification {

    public static Specification<PriceAdjustment> searchWith(PriceAdjustmentSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 호텔 ID로 검색 (RatePlan을 통한 조인)
            if (request.getHotelId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("ratePlan").get("roomType").get("hotel").get("id"), request.getHotelId()));
            }

            // 룸 타입 ID로 검색
            if (request.getRoomTypeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("ratePlan").get("roomType").get("id"), request.getRoomTypeId()));
            }

            // 요금제 ID로 검색
            if (request.getRatePlanId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("ratePlan").get("id"), request.getRatePlanId()));
            }

            // 가격 조정 정책 이름으로 검색 (부분 일치)
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + request.getName().toLowerCase() + "%"
                ));
            }

            // 조정 타입 (할인/할증)
            if (request.getAdjustmentType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("adjustmentType"), request.getAdjustmentType()));
            }

            // 계산 타입 (정액/정률)
            if (request.getCalculationType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("calculationType"), request.getCalculationType()));
            }

            // 적용 기간 검색 (시작일과 종료일 범위)
            if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("endDate"), request.getStartDate()
                ));
            }

            if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("startDate"), request.getEndDate()
                ));
            }

            // 상태
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            } else {
                // 기본적으로 INACTIVE 상태는 제외
                predicates.add(criteriaBuilder.notEqual(root.get("status"), Status.INACTIVE));
            }

            // 예약 일정 조건 (얼리버드/라스트미닛)
            if (request.getBookingDaysBeforeArrival() != null) {
                predicates.add(criteriaBuilder.equal(root.get("bookingDaysBeforeArrival"), request.getBookingDaysBeforeArrival()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
