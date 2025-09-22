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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
// 특정 객실 타입의 특정 날짜 재고는 유일해야 하므로 복합 유니크 제약조건 설정
@Table(name = "room_inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_type_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomInventory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int totalQuantity; // 총 재고량

    @Column(nullable = false)
    private int reservedQuantity; // 예약된 수량

    /**
     * 동시성 제어를 위한 버전 필드 (Optimistic Lock)
     * 재고 차감 시 이 버전을 함께 검사하여 다른 트랜잭션이 먼저 재고를 수정했는지 확인합니다.
     */
    @Version
    private Integer version;

    @Builder
    public RoomInventory(RoomType roomType, LocalDate date, int totalQuantity) {
        this.roomType = roomType;
        this.date = date;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = 0; // 초기 예약 수량은 0
    }

    //== 비즈니스 로직 ==//
    /**
     * 재고를 감소시킵니다. (예약 시 사용)
     * @param quantity 감소시킬 수량
     */
    public void decreaseStock(int quantity) {
        int restStock = this.totalQuantity - this.reservedQuantity;
        if (restStock < quantity) {
            throw new IllegalStateException("Not enough stock available."); // 재고 부족 예외
        }
        this.reservedQuantity += quantity;
    }

    /**
     * 재고를 증가시킵니다. (예약 취소 시 사용)
     * @param quantity 증가시킬 수량
     */
    public void increaseStock(int quantity) {
        this.reservedQuantity -= quantity;
    }
}
