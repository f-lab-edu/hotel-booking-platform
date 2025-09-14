package dev.muho.hotel.domain.roominventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "room_inventories")
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_rooms", nullable = false)
    private Integer totalRooms;

    @Column(name = "available_rooms", nullable = false)
    private Integer availableRooms;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private RoomInventory(Long id, Long roomTypeId, LocalDate date, Integer totalRooms, Integer availableRooms, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.date = date;
        this.totalRooms = totalRooms;
        this.availableRooms = availableRooms;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static RoomInventory of(Long roomTypeId, LocalDate date, Integer totalRooms, Integer availableRooms) {
        return RoomInventory.builder()
                .roomTypeId(roomTypeId)
                .date(date)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(Integer totalRooms, Integer availableRooms) {
        if (totalRooms != null) this.totalRooms = totalRooms;
        if (availableRooms != null) this.availableRooms = availableRooms;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
}
