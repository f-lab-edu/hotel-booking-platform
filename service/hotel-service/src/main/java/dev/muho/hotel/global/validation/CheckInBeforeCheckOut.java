package dev.muho.hotel.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CheckInBeforeCheckOutValidator.class) // 2. 검증 로직을 담고 있는 클래스 지정
@Target(ElementType.TYPE) // 1. 클래스 레벨에 붙이는 어노테이션
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInBeforeCheckOut {
    String message() default "체크아웃 날짜는 체크인 날짜보다 늦어야 합니다."; // 3. 유효성 검사 실패 시 반환될 메시지
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
