package dev.muho.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
// 특정 요금제의 특정 날짜 기본 요금은 유일해야 함
@Table(name = "base_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"rate_plan_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseRate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_plan_id", nullable = false)
    private RatePlan ratePlan;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 조정이 적용되기 전의 기본 가격

    @Builder
    public BaseRate(RatePlan ratePlan, LocalDate date, BigDecimal price) {
        this.ratePlan = ratePlan;
        this.date = date;
        this.price = price;
    }
}
