package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomTypeUpdateRequest {

    @NotBlank(message = "객실 타입명은 필수입니다")
    private String name;

    @NotNull(message = "기준 인원은 필수입니다")
    @Min(value = 1, message = "기준 인원은 1명 이상이어야 합니다")
    private Integer standardCapacity;

    @NotNull(message = "최대 인원은 필수입니다")
    @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다")
    private Integer maxCapacity;

    @NotNull(message = "상태는 필수입니다")
    private Status status;
}
