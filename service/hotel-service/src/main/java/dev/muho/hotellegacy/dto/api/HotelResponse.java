package dev.muho.hotellegacy.dto.api;

import dev.muho.hotellegacy.dto.command.HotelInfoResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
    private String country;
    private String city;
    private Integer rating;
    private String description;
    private String contactNumber;

    public static HotelResponse from(HotelInfoResult r) {
        return new HotelResponse(
                r.id(),
                r.name(),
                r.address(),
                r.country(),
                r.city(),
                r.rating(),
                r.description(),
                r.contactNumber()
        );
    }
}
