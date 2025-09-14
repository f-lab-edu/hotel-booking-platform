package dev.muho.booking.domain.booking.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true, length = 100)
    private String bookingId; // external UUID or similar

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "hotel_name")
    private String hotelName;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "room_type_name")
    private String roomTypeName;

    @Column(name = "rate_plan_id", nullable = false)
    private Long ratePlanId;

    @Column(name = "rate_plan_name")
    private String ratePlanName;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Booking(Long id, String bookingId, BookingStatus status, Long userId, String guestName,
                    Long hotelId, String hotelName, Long roomTypeId, String roomTypeName, Long ratePlanId, String ratePlanName,
                    LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount,
                    BigDecimal basePrice, BigDecimal finalPrice,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.status = status == null ? BookingStatus.RESERVED : status;
        this.userId = userId;
        this.guestName = guestName;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.ratePlanId = ratePlanId;
        this.ratePlanName = ratePlanName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Booking createNew(String bookingId, Long userId, String guestName,
                                    Long hotelId, String hotelName, Long roomTypeId, String roomTypeName,
                                    Long ratePlanId, String ratePlanName,
                                    LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount,
                                    BigDecimal basePrice, BigDecimal finalPrice) {
        return Booking.builder()
                .bookingId(bookingId)
                .status(BookingStatus.RESERVED)
                .userId(userId)
                .guestName(guestName)
                .hotelId(hotelId)
                .hotelName(hotelName)
                .roomTypeId(roomTypeId)
                .roomTypeName(roomTypeName)
                .ratePlanId(ratePlanId)
                .ratePlanName(ratePlanName)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guestCount(guestCount)
                .basePrice(basePrice)
                .finalPrice(finalPrice)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateStatus(BookingStatus newStatus) {
        if (newStatus == null) return;
        if (this.status == newStatus) return;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBookingDetails(String guestName, LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount,
                                     BigDecimal basePrice, BigDecimal finalPrice) {
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
}

