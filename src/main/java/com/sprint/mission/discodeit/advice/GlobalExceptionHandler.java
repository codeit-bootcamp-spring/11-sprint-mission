package com.sprint.mission.discodeit.advice;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.response.ErrorResponse;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
        .body(ErrorResponse.from(ErrorCode.VALIDATION_ERROR, details, e.getClass()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handle(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.from(ErrorCode.FORBIDDEN, Map.of("message", e.getMessage()),
            e.getClass()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
    log.error("예상치 못한 예외", e);
    return ResponseEntity.internalServerError()
        .body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(), e.getClass()));
  }
}
