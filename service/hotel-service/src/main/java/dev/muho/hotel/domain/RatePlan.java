package dev.muho.hotel.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rate_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatePlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private String name; // 예: "얼리버드 특가", "조식 포함"

    /**
     * 현재 판매 가능 여부를 나타냅니다.
     * false일 경우, 사용자에게 노출되거나 예약되지 않습니다.
     */
    @Column(nullable = false)
    private boolean onSale = true;

    /**
     * 이 요금제로 예약하기 위한 최소 숙박일 조건입니다.
     * 0 또는 1은 조건 없음을 의미합니다.
     */
    @Column(nullable = false)
    private int minNights = 1;

    /**
     * 이 요금제로 예약할 수 있는 최대 숙박일 조건입니다.
     */
    private Integer maxNights;

    /**
     * 이 요금제를 예약할 수 있는 기간의 시작일입니다. (Booking Window)
     * 예: 9월 한정 판매 상품
     */
    private LocalDate bookingStartDate;

    /**
     * 이 요금제를 예약할 수 있는 기간의 종료일입니다.
     */
    private LocalDate bookingEndDate;

    /**
     * 실제 숙박(체크인)이 가능한 기간의 시작일입니다. (Stay Window)
     * 예: 겨울 시즌(12월~2월) 숙박 전용 상품
     */
    private LocalDate checkInStartDate;

    /**
     * 실제 숙박(체크인)이 가능한 기간의 종료일입니다.
     */
    private LocalDate checkInEndDate;

    @OneToMany(mappedBy = "ratePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BaseRate> baseRates = new ArrayList<>();

    @OneToMany(mappedBy = "ratePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceAdjustment> priceAdjustments = new ArrayList<>();

    @Builder
    public RatePlan(RoomType roomType,
                    String name,
                    boolean onSale,
                    int minNights,
                    Integer maxNights,
                    LocalDate bookingStartDate,
                    LocalDate bookingEndDate,
                    LocalDate checkInStartDate,
                    LocalDate checkInEndDate) {
        this.roomType = roomType;
        this.name = name;
        this.onSale = onSale;
        this.minNights = minNights;
        this.maxNights = maxNights;
        this.bookingStartDate = bookingStartDate;
        this.bookingEndDate = bookingEndDate;
        this.checkInStartDate = checkInStartDate;
        this.checkInEndDate = checkInEndDate;
    }
}
