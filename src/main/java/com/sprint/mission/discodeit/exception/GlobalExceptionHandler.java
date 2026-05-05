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

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handle(DiscodeitException ex) {
    log.warn("DiscodeitException: {}", ex.getMessage());
    return ResponseEntity
        .status(ex.getErrorCode().getStatus())
        .body(ErrorResponse.from(ex));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {

    Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값",
            (a, b) -> a
        ));

    log.warn("Validation Error: {}", details);

    return ResponseEntity.badRequest()
        .body(new ErrorResponse(400, "ValidationError", "유효성 검사 실패", details));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
    log.error("예상치 못한 예외 발생", ex);
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(500, ex.getClass().getSimpleName(), "서버 오류가 발생했습니다.", Map.of()));
  }
}