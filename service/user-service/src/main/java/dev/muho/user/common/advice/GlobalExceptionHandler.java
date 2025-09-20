package dev.muho.user.common.advice;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;
import dev.muho.user.common.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse response = ErrorResponse.validation(errors);
        return ResponseEntity.badRequest().body(response);
    }

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        // detail 로깅 분리
        if (e.getDetail() != null) {
            log.warn("BusinessException occurred: code={}, message={}, detail={}", errorCode.getCode(), errorCode.getMessage(), e.getDetail());
        } else {
            log.warn("BusinessException occurred: code={}, message={}", errorCode.getCode(), errorCode.getMessage());
        }
        ErrorResponse response = (e.getDetail() == null) ? ErrorResponse.of(errorCode) : ErrorResponse.of(errorCode, e.getDetail());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}
