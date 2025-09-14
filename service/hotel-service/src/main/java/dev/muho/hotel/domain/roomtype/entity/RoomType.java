package dev.muho.hotel.domain.roomtype.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_occupancy", nullable = false)
    private Integer maxOccupancy;

    @Column(name = "standard_occupancy", nullable = false)
    private Integer standardOccupancy;

    @Column(name = "view_type")
    private String viewType;

    @Column(name = "bed_type")
    private String bedType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private RoomType(Long id, Long hotelId, String name, String description,
                     Integer maxOccupancy, Integer standardOccupancy,
                     String viewType, String bedType,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.name = name;
        this.description = description;
        this.maxOccupancy = maxOccupancy;
        this.standardOccupancy = standardOccupancy;
        this.viewType = viewType;
        this.bedType = bedType;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static RoomType createNew(Long hotelId, String name, String description, Integer maxOccupancy, Integer standardOccupancy, String viewType, String bedType) {
        return RoomType.builder()
                .hotelId(hotelId)
                .name(name)
                .description(description)
                .maxOccupancy(maxOccupancy)
                .standardOccupancy(standardOccupancy)
                .viewType(viewType)
                .bedType(bedType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String description, Integer maxOccupancy, Integer standardOccupancy, String viewType, String bedType) {
        this.name = name;
        this.description = description;
        this.maxOccupancy = maxOccupancy;
        this.standardOccupancy = standardOccupancy;
        this.viewType = viewType;
        this.bedType = bedType;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
}
