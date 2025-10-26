package dev.muho.hoteleventconsumer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "room_inventories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int totalQuantity; // 총 재고량

    @Column(nullable = false)
    private int reservedQuantity; // 예약된 수량

    /**
     * 재고를 감소시킵니다. (예약 시 사용)
     */
    public void reserveRoom() {
        int quantity = 1; // 한 번에 1개의 객실 예약
        int restStock = this.totalQuantity - this.reservedQuantity;
        if (restStock < quantity) {
            throw new IllegalStateException("Not enough stock available."); // 재고 부족 예외
        }
        this.reservedQuantity += quantity;
    }

    /**
     * 재고를 증가시킵니다. (예약 취소 시 사용)
     */
    public void cancelRoom() {
        int quantity = 1; // 한 번에 1개의 객실 취소
        this.reservedQuantity -= quantity;
    }
}
