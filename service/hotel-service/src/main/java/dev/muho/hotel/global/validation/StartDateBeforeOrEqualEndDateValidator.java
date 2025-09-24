package dev.muho.hotel.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class StartDateBeforeOrEqualEndDateValidator implements ConstraintValidator<StartDateBeforeOrEqualEndDate, DateRangeValidatable> {

    @Override
    public boolean isValid(DateRangeValidatable request, ConstraintValidatorContext context) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // 두 날짜 중 하나라도 null이면 @NotNull 어노테이션이 먼저 처리하므로, 여기서는 유효하다고 간주
        if (startDate == null || endDate == null) {
            return true;
        }

        // 시작 날짜가 종료 날짜보다 이전(before)이거나 같은(equal) 경우 유효
        return !startDate.isAfter(endDate);
    }
}
