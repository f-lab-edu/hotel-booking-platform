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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomType extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 설정
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int standardCapacity; // 기준 인원

    @Column(nullable = false)
    private int maxCapacity; // 최대 인원

    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomInventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RatePlan> ratePlans = new ArrayList<>();

    @Builder
    public RoomType(Hotel hotel, String name, int standardCapacity, int maxCapacity) {
        this.hotel = hotel;
        this.name = name;
        this.standardCapacity = standardCapacity;
        this.maxCapacity = maxCapacity;
    }

    /** 룸타입 정보 업데이트 메서드 */
    public void update(String name, int standardCapacity, int maxCapacity, Status status) {
        this.name = name;
        this.standardCapacity = standardCapacity;
        this.maxCapacity = maxCapacity;
        this.status = status;
    }

    /** 룸타입 상태 변경 메서드 */
    public void changeStatus(Status status) {
        this.status = status;
    }

    /** 인원 수가 룸타입의 최대 수용 인원을 초과하는지 여부를 확인하는 메서드 */
    public boolean validateCapacity(int numOfAdult, int numOfChildren) {
        return this.maxCapacity >= numOfAdult + numOfChildren;
    }

    public boolean isOnSale() {
        return this.status == Status.ACTIVE;
    }
}
