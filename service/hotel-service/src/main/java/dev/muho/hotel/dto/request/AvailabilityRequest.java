package dev.muho.hotel.dto.request;


import dev.muho.hotel.global.validation.CheckInBeforeCheckOut;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@CheckInBeforeCheckOut
public class AvailabilityRequest {

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @FutureOrPresent(message = "체크인 날짜는 오늘이거나 오늘 이후여야 합니다.")
    private LocalDate checkInDate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    @Min(value = 1, message = "성인 수는 최소 1명 이상이어야 합니다.")
    private int adults;

    @Min(value = 0, message = "아동 수는 0명 이상이어야 합니다.")
    private int children;
}
