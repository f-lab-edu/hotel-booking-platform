package dev.muho.hotel.domain.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotelResponse {
    private Long hotelId;
    private String name;
    private String address;
    private String country;
    private String city;
    private Integer rating;
    private String description;
    private String contactNumber;
}
