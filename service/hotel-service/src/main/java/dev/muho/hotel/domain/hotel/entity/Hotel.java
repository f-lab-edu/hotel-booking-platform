package dev.muho.hotel.domain.hotel.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String description;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Hotel(Long id, String name, String address, String country, String city,
                  Integer rating, String description, String contactNumber,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateName(name);
        validateAddress(address);
        validateCountry(country);
        validateCity(city);
        validateRating(rating);
        this.id = id;
        this.name = name;
        this.address = address;
        this.country = country;
        this.city = city;
        this.rating = rating;
        this.description = description;
        this.contactNumber = contactNumber;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Hotel createNew(String name, String address, String country, String city,
                                  Integer rating, String description, String contactNumber) {
        return Hotel.builder()
                .name(name)
                .address(address)
                .country(country)
                .city(city)
                .rating(rating)
                .description(description)
                .contactNumber(contactNumber)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String address, String country, String city,
                       Integer rating, String description, String contactNumber) {
        validateName(name);
        validateAddress(address);
        validateCountry(country);
        validateCity(city);
        validateRating(rating);
        this.name = name;
        this.address = address;
        this.country = country;
        this.city = city;
        this.rating = rating;
        this.description = description;
        this.contactNumber = contactNumber;
        this.updatedAt = LocalDateTime.now();
    }

    // 검증 메서드들 (간단히 구현)
    private static void validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("호텔 이름은 필수입니다.");
        if (name.length() > 200) throw new IllegalArgumentException("호텔 이름이 너무 깁니다.");
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("주소는 필수입니다.");
    }

    private static void validateCountry(String country) {
        if (country == null || country.isBlank()) throw new IllegalArgumentException("국가는 필수입니다.");
    }

    private static void validateCity(String city) {
        if (city == null || city.isBlank()) throw new IllegalArgumentException("도시는 필수입니다.");
    }

    private static void validateRating(Integer rating) {
        if (rating == null) throw new IllegalArgumentException("호텔 등급은 필수입니다.");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("호텔 등급은 1~5 사이여야 합니다.");
    }

    // id 설정(영속 계층에서 사용)
    public void setId(Long id) {
        this.id = id;
    }
}
