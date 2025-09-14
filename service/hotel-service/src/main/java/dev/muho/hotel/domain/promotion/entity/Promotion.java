package dev.muho.hotel.domain.promotion.entity;

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
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long id;

    @Column(name = "applicable_hotel_id")
    private Long applicableHotelId;

    @Column(name = "applicable_room_type_id")
    private Long applicableRoomTypeId;

    @Column(name = "applicable_rate_plan_id")
    private Long applicableRatePlanId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 50)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_nights")
    private Integer minNights;

    @Column(name = "booking_window_min_days")
    private Integer bookingWindowMinDays;

    @Column(name = "booking_window_max_days")
    private Integer bookingWindowMaxDays;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Promotion(Long id, Long applicableHotelId, Long applicableRoomTypeId, Long applicableRatePlanId,
                      String name, String description, DiscountType discountType, BigDecimal discountValue,
                      Integer minNights, Integer bookingWindowMinDays, Integer bookingWindowMaxDays,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.applicableHotelId = applicableHotelId;
        this.applicableRoomTypeId = applicableRoomTypeId;
        this.applicableRatePlanId = applicableRatePlanId;
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minNights = minNights;
        this.bookingWindowMinDays = bookingWindowMinDays;
        this.bookingWindowMaxDays = bookingWindowMaxDays;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Promotion createNew(Long applicableHotelId, Long applicableRoomTypeId, Long applicableRatePlanId,
                                      String name, String description, DiscountType discountType, java.math.BigDecimal discountValue,
                                      Integer minNights, Integer bookingWindowMinDays, Integer bookingWindowMaxDays) {
        return Promotion.builder()
                .applicableHotelId(applicableHotelId)
                .applicableRoomTypeId(applicableRoomTypeId)
                .applicableRatePlanId(applicableRatePlanId)
                .name(name)
                .description(description)
                .discountType(discountType)
                .discountValue(discountValue)
                .minNights(minNights)
                .bookingWindowMinDays(bookingWindowMinDays)
                .bookingWindowMaxDays(bookingWindowMaxDays)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String description, DiscountType discountType, BigDecimal discountValue,
                       Integer minNights, Integer bookingWindowMinDays, Integer bookingWindowMaxDays) {
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minNights = minNights;
        this.bookingWindowMinDays = bookingWindowMinDays;
        this.bookingWindowMaxDays = bookingWindowMaxDays;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
}
