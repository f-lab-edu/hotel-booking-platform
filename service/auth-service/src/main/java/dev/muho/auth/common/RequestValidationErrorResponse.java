package dev.muho.auth.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class RequestValidationErrorResponse {

    private final String code;
    private final String message;
    private final Map<String, String> validation; // 필드별 에러 메시지를 담을 Map
}
