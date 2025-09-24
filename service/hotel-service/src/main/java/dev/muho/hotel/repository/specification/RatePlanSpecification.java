package dev.muho.hotel.repository.specification;

import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RatePlanSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RatePlanSpecification {

    public static Specification<RatePlan> searchWith(RatePlanSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 룸 타입 ID로 검색
            if (request.getRoomTypeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("roomType").get("id"), request.getRoomTypeId()));
            }

            // 요금제 이름으로 검색 (부분 일치)
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + request.getName().toLowerCase() + "%"
                ));
            }

            // 조식 포함 여부
            if (request.getIncludesBreakfast() != null) {
                predicates.add(criteriaBuilder.equal(root.get("includesBreakfast"), request.getIncludesBreakfast()));
            }

            // 환불 가능 여부
            if (request.getRefundable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("refundable"), request.getRefundable()));
            }

            // 상태
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            } else {
                // 기본적으로 INACTIVE 상태는 제외
                predicates.add(criteriaBuilder.notEqual(root.get("status"), Status.INACTIVE));
            }

            // 예약 가능 기간 검색
            if (request.getBookingStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("bookingEndDate"), request.getBookingStartDate()
                ));
            }

            if (request.getBookingEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("bookingStartDate"), request.getBookingEndDate()
                ));
            }

            // 체크인 가능 기간 검색
            if (request.getCheckInStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("checkInEndDate"), request.getCheckInStartDate()
                ));
            }

            if (request.getCheckInEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("checkInStartDate"), request.getCheckInEndDate()
                ));
            }

            // 최소/최대 숙박일 검색
            if (request.getMinNights() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("minNights"), request.getMinNights()));
            }

            if (request.getMaxNights() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxNights"), request.getMaxNights()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
