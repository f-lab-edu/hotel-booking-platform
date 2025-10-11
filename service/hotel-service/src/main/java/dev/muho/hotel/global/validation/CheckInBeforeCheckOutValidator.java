package dev.muho.hotel.global.validation;

import dev.muho.hotel.dto.request.AvailabilityRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class CheckInBeforeCheckOutValidator implements ConstraintValidator<CheckInBeforeCheckOut, AvailabilityRequest> {

    @Override
    public boolean isValid(AvailabilityRequest request, ConstraintValidatorContext context) {
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        // 두 날짜 중 하나라도 null이면 @NotNull 어노테이션이 먼저 처리하므로, 여기서는 유효하다고 간주
        if (checkIn == null || checkOut == null) {
            return true;
        }

        // 체크인 날짜가 체크아웃 날짜보다 이전(before)이어야 유효
        return checkIn.isBefore(checkOut);
    }
}
