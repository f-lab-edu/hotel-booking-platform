package dev.muho.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_adjustments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceAdjustment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_plan_id", nullable = false)
    private RatePlan ratePlan;

    @Column(nullable = false)
    private String name; // 예: "성수기 할증", "오픈 특가 할인"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentType adjustmentType; // 할인(DISCOUNT)인지 할증(SURCHARGE)인지 구분

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalculationType calculationType; // 정액(FIXED_AMOUNT)인지 정률(PERCENTAGE)인지 구분

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // 조정 값 (예: 20000, 10.5)

    @Column(nullable = false)
    private LocalDate startDate; // 이 정책이 적용되는 기간 (시작)

    @Column(nullable = false)
    private LocalDate endDate; // 이 정책이 적용되는 기간 (종료)

    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // 얼리버드/라스트미닛 조건을 위한 필드
    // 예: 30 -> 체크인 30일 이전 예약 시 적용
    private Integer bookingDaysBeforeArrival;

    @Builder
    public PriceAdjustment(RatePlan ratePlan,
                           String name,
                           AdjustmentType adjustmentType,
                           CalculationType calculationType,
                           BigDecimal amount,
                           LocalDate startDate,
                           LocalDate endDate,
                           Integer bookingDaysBeforeArrival) {
        this.ratePlan = ratePlan;
        this.name = name;
        this.adjustmentType = adjustmentType;
        this.calculationType = calculationType;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookingDaysBeforeArrival = bookingDaysBeforeArrival;
    }

    public void update(String name,
                       AdjustmentType adjustmentType,
                       CalculationType calculationType,
                       BigDecimal amount,
                       LocalDate startDate,
                       LocalDate endDate,
                       Status status,
                       Integer bookingDaysBeforeArrival) {
        this.name = name;
        this.adjustmentType = adjustmentType;
        this.calculationType = calculationType;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.bookingDaysBeforeArrival = bookingDaysBeforeArrival;
    }
}
