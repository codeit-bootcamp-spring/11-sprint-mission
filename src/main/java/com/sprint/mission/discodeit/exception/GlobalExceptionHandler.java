package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    ErrorResponse response = new ErrorResponse(e);
    log.error("[Exception] Code: {}, Message: {}, Details: {}",
            response.getCode(), response.getMessage(), response.getDetails(), e);
    return ResponseEntity
            .status(response.getStatus())
            .body(response);
  }

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorResponse response = new ErrorResponse(e);
    log.warn("[DiscodeitException] Code: {}, Message: {}, Details: {}",
            response.getCode(), response.getMessage(), response.getDetails());
    return ResponseEntity
            .status(response.getStatus())
            .body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    ErrorResponse response = new ErrorResponse(e);
    log.warn("[ValidationException] Code: {}, Message: {}, Details: {}",
            response.getCode(), response.getMessage(), response.getDetails());
    return ResponseEntity
            .status(response.getStatus())
            .body(response);
  }

  // TODO: 추후 삭제
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("[IllegalArgumentException] Message: {}", e.getMessage());

    ErrorResponse response = new ErrorResponse(
            e, "BAD-REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST);
    return ResponseEntity.status(response.getStatus()).body(response);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
    log.warn("[NoSuchElementException] Message: {}", e.getMessage());

    ErrorResponse response = new ErrorResponse(
            e, "NOT-FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
    return ResponseEntity.status(response.getStatus()).body(response);
  }

}
