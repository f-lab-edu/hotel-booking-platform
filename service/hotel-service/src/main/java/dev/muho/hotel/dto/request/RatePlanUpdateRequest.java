package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RatePlanUpdateRequest {

    @NotBlank(message = "요금제 이름은 필수입니다")
    private String name;

    @Min(value = 1, message = "최소 숙박일은 1 이상이어야 합니다")
    private Integer minNights;

    private Integer maxNights;

    @NotNull(message = "조식 포함 여부는 필수입니다")
    private Boolean includesBreakfast;

    @NotNull(message = "환불 가능 여부는 필수입니다")
    private Boolean refundable;

    @NotNull(message = "상태는 필수입니다")
    private Status status;

    private LocalDate bookingStartDate;

    private LocalDate bookingEndDate;

    private LocalDate checkInStartDate;

    private LocalDate checkInEndDate;
}
