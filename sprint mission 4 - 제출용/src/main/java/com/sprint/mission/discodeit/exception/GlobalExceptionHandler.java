package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 400 - null/blank(서비스에서 체크)
  @ExceptionHandler(DiscodeitInvalidInputException.class)
  public ResponseEntity<ErrorResponse> handleInvalidInput(DiscodeitInvalidInputException e) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 400 - @Valid를 DTO에 다는경우
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

    String message = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  //401 - 로그인 시 비밀번호 불일치
  @ExceptionHandler(DiscodeitInvalidPasswordException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPassword(DiscodeitException e) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.UNAUTHORIZED.value(),
        "Unauthorized",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  // 404 - 조회 실패
  @ExceptionHandler(DiscodeitNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(DiscodeitException e) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  // 409 - 중복 체크
  @ExceptionHandler(DiscodeitDuplicateException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(DiscodeitException e) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.CONFLICT.value(),
        "Conflict",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  // 500 - 잡히지 않은 모든 에러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
