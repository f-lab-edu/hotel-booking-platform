package dev.muho.auth.common;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RequestValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // 1. 에러 메시지를 담을 Map을 생성합니다.
        Map<String, String> errors = new HashMap<>();

        // 2. 발생한 모든 FieldError에 대해 반복하며 Map에 추가합니다.
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 3. ErrorResponse DTO를 생성하여 응답합니다.
        RequestValidationErrorResponse response = new RequestValidationErrorResponse("BAD_REQUEST", "입력값이 유효하지 않습니다.", errors);

        // 4. HTTP 상태 코드 400 (Bad Request)와 함께 응답 DTO를 반환합니다.
        return ResponseEntity.badRequest().body(response);
    }
}
