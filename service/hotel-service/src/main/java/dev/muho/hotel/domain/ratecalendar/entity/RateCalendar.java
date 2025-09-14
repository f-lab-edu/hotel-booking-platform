package dev.muho.hotel.domain.ratecalendar.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "rate_calendars")
public class RateCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_calendar_id")
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "rate_calendar_applicable_days", joinColumns = @JoinColumn(name = "rate_calendar_id"))
    @Column(name = "day_of_week", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> applicableDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", length = 50)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_value")
    private BigDecimal adjustmentValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private RateCalendar(Long id, Long hotelId, String name, String description,
                         LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                         AdjustmentType adjustmentType, BigDecimal adjustmentValue,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicableDays = applicableDays;
        this.adjustmentType = adjustmentType;
        this.adjustmentValue = adjustmentValue;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static RateCalendar createNew(Long hotelId, String name, String description,
                                         LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                                         AdjustmentType adjustmentType, BigDecimal adjustmentValue) {
        return RateCalendar.builder()
                .hotelId(hotelId)
                .name(name)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .applicableDays(applicableDays)
                .adjustmentType(adjustmentType)
                .adjustmentValue(adjustmentValue)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String description, LocalDate startDate, LocalDate endDate,
                       Set<DayOfWeek> applicableDays, AdjustmentType adjustmentType, BigDecimal adjustmentValue) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicableDays = applicableDays;
        this.adjustmentType = adjustmentType;
        this.adjustmentValue = adjustmentValue;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
}
