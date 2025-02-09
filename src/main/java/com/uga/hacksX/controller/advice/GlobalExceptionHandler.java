package com.uga.hacksX.controller.advice;

import com.uga.hacksX.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientResponseException(WebClientResponseException ex) {
        String errorMessage = switch (ex.getStatusCode().value()) {
            case 404 -> "요청한 데이터를 찾을 수 없습니다.";
            case 429 -> "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";
            case 503 -> "외부 서비스가 일시적으로 불가능합니다.";
            default -> "외부 API 호출 중 오류가 발생했습니다.";
        };
        
        log.error("API call failed: {} - {}", ex.getStatusCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", errorMessage,
                    "code", String.valueOf(ex.getStatusCode().value())
                ));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, String>> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "외부 서비스 연동 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid input: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "잘못된 입력값입니다: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
    }
} 