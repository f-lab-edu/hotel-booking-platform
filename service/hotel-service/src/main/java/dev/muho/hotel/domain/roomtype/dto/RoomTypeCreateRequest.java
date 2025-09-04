package dev.muho.hotel.domain.roomtype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RoomTypeCreateRequest {

    @NotBlank(message = "객실 타입 이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    private String name;

    private String description;

    @NotNull(message = "최대 인원은 필수입니다.")
    @Positive(message = "최대 인원은 1 이상이어야 합니다.")
    private Integer maxOccupancy;

    @NotNull(message = "기준 인원은 필수입니다.")
    @Positive(message = "기준 인원은 1 이상이어야 합니다.")
    private Integer standardOccupancy;

    private String viewType;

    private String bedType;
}
