package dev.muho.hotel.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class HotelUpdateRequest {

    @NotBlank(message = "호텔명은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "등급은 필수입니다.")
    @Min(value = 1, message = "등급은 1 이상이어야 합니다.")
    @Max(value = 5, message = "등급은 5 이하여야 합니다.")
    private Integer rating;
}
