package dev.muho.hotel.domain.hotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class HotelCreateRequest {

    @NotBlank(message = "호텔 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotBlank(message = "국가는 필수입니다.")
    private String country;

    @NotBlank(message = "도시는 필수입니다.")
    private String city;

    @NotNull(message = "호텔 등급은 필수입니다.")
    @Min(value = 1, message = "호텔 등급은 1성 이상이어야 합니다.")
    @Max(value = 5, message = "호텔 등급은 5성을 초과할 수 없습니다.")
    private Integer rating;

    private String description;

    private String contactNumber;
}
