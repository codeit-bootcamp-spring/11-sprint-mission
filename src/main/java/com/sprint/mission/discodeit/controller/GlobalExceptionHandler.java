package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleException(DiscodeitException e) {

    log.warn("Discodeit Exception: {}", e.getMessage());

    ErrorCode errorCode = e.getErrorCode();

    ErrorResponse errorResponse = new ErrorResponse(
        e.getTimestamp(),
        "Discodeit Exception",
        errorCode.getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        errorCode.getStatus().value()
    );

    return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
  }


  @ExceptionHandler()
  public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {

    Map<String, Object> details = new HashMap<>();

    e.getBindingResult().getFieldErrors().forEach(error -> {
      details.put(error.getField(), error.getDefaultMessage());
    });

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "INVALID_INPUT",
        e.getMessage(),
        details,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);


  }

  @ExceptionHandler()
  public ResponseEntity<ErrorResponse> handleException(IllegalArgumentException e) {

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "Illegal Arguments",
        e.getMessage(),
        Map.of(),
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.status(400).body(errorResponse);
  }


  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handle(AccessDeniedException e) {
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "X",
        e.getMessage(),
        Map.of(),
        e.getClass().getSimpleName(),
        HttpStatus.FORBIDDEN.value()
    );

    return ResponseEntity.status(403).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {

    log.error("예상치 못한 예외", e);

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "Internal Server Error",
        e.getMessage(),
        Map.of(),
        e.getClass().getSimpleName(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()

    );

    return ResponseEntity.status(500).body(errorResponse);
  }


}
