package dev.muho.hotel.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hotel extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int rating; // 성급

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotelStatus status = HotelStatus.OPERATING; // 기본값: 운영 중

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomType> roomTypes = new ArrayList<>();

    @Builder
    public Hotel(String name, String address, int rating) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.status = HotelStatus.OPERATING;
    }

    /** 호텔 정보 업데이트 메서드 */
    public void updateHotelInfo(String name, String address, int rating) {
        this.name = name;
        this.address = address;
        this.rating = rating;
    }

    /** 호텔 상태 변경 메서드 (논리적 삭제 포함) */
    public void changeStatus(HotelStatus status) {
        this.status = status;
    }
}
