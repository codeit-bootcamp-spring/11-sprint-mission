package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // DTO 검증
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값",
                        (a, b) -> a
                ));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "ValidationError", "유효성 검사 실패", details));
    }

    // 지정 예외
    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
        if(e.getErrorCode().getStatus().is5xxServerError()) {
            log.error("DiscodeitException: type={}, message={}, details={}",
                    e.getClass().getSimpleName(), e.getMessage(), e.getDetails(), e);
        } else {
            log.warn("DiscodeitException: type={}, message={}, details={}",
                    e.getClass().getSimpleName(), e.getMessage(), e.getDetails());
        }

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.from(e));
    }

    // 비지정 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception e) {
        log.error("예상치 못한 예외", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(500, e.getClass().getSimpleName(), "서버 오류가 발생했습니다.", Map.of()));
    }
}
