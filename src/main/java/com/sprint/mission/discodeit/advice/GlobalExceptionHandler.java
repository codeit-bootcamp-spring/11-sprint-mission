package com.sprint.mission.discodeit.advice;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.response.ErrorResponse;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleBusinessLogicException(DiscodeitException e) {
    log.warn("DiscodeitException: {}", e.getMessage());
    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(ErrorResponse.from(e));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
    Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField,
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값",
            (a, b) -> a));

    return ResponseEntity.badRequest()
        .body(new ErrorResponse(
            Instant.now(),
            "VALIDATION_ERROR",
            "유효성 검사 실패",
            details,
            e.getClass().getSimpleName(),
            400
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
    log.error("예상치 못한 예외", e);
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(
            Instant.now(),
            "INTERNAL_SERVER_ERROR",
            "서버 오류가 발생했습니다.",
            Map.of(),
            e.getClass().getSimpleName(),
            500
        ));
  }
}
