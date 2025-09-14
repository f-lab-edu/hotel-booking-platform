package dev.muho.hotel.domain.rateplan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "rate_plans")
public class RatePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_plan_id")
    private Long id;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "breakfast_included")
    private boolean breakfastIncluded;

    @Column(name = "refundable")
    private boolean refundable;

    @Column(name = "min_nights")
    private Integer minNights;

    @Column(name = "max_nights")
    private Integer maxNights;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private RatePlan(Long id, Long roomTypeId, String name, String description, BigDecimal basePrice,
                     boolean breakfastIncluded, boolean refundable, Integer minNights, Integer maxNights,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.breakfastIncluded = breakfastIncluded;
        this.refundable = refundable;
        this.minNights = minNights;
        this.maxNights = maxNights;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static RatePlan createNew(Long roomTypeId, String name, String description, BigDecimal basePrice,
                                     boolean breakfastIncluded, boolean refundable, Integer minNights, Integer maxNights) {
        return RatePlan.builder()
                .roomTypeId(roomTypeId)
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .breakfastIncluded(breakfastIncluded)
                .refundable(refundable)
                .minNights(minNights)
                .maxNights(maxNights)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String description, BigDecimal basePrice, boolean breakfastIncluded,
                       boolean refundable, Integer minNights, Integer maxNights) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.breakfastIncluded = breakfastIncluded;
        this.refundable = refundable;
        this.minNights = minNights;
        this.maxNights = maxNights;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
}
