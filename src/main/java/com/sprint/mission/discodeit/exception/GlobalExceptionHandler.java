package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.common.UnexpectedErrorException;
import com.sprint.mission.discodeit.exception.common.ValidationErrorException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            fe -> fe.getField(),
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : ""
        ));
    DiscodeitException exception = ValidationErrorException.withDetails(details);
    log.warn("Validation failed: {}", details);
    return ResponseEntity
        .status(exception.getErrorCode().getHttpStatus())
        .body(ErrorResponse.from(exception));
  }

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("DiscodeitException: code={}, message={}", errorCode.getCode(),
        errorCode.getMessage());
    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(ErrorResponse.from(e));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    DiscodeitException exception = UnexpectedErrorException.withCause();
    log.error("Unexpected error: {}", e.getMessage(), e);
    return ResponseEntity
        .status(exception.getErrorCode().getHttpStatus())
        .body(ErrorResponse.from(exception));
  }
}