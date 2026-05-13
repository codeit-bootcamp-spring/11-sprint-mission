package com.sprint.mission.discodeit.exception;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseDto.of(HttpStatus.NOT_FOUND.value(), e, e.getDetails()));
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseDto.of(HttpStatus.CONFLICT.value(), e, e.getDetails()));
  }

  @ExceptionHandler(ChannelNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleChannelNotFound(ChannelNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseDto.of(HttpStatus.NOT_FOUND.value(), e, e.getDetails()));
  }

  @ExceptionHandler(ChannelUpdateNotAllowedException.class)
  public ResponseEntity<ErrorResponseDto> handleChannelUpdateNotAllowed(
      ChannelUpdateNotAllowedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponseDto.of(HttpStatus.FORBIDDEN.value(), e, e.getDetails()));
  }

  @ExceptionHandler(MessageNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleMessageNotFound(MessageNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseDto.of(HttpStatus.NOT_FOUND.value(), e, e.getDetails()));
  }

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponseDto> handleDiscodeit(DiscodeitException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponseDto.of(HttpStatus.BAD_REQUEST.value(), e, e.getDetails()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDto> handleAuth(AuthenticationException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponseDto.of(HttpStatus.UNAUTHORIZED.value(), e, Map.of()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponseDto.of(HttpStatus.FORBIDDEN.value(), e, Map.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            e.getClass().getSimpleName(),
            msg,
            Map.of(),
            java.time.Instant.now()
        ));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException e) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ErrorResponseDto.of(HttpStatus.METHOD_NOT_ALLOWED.value(), e, Map.of()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponseDto.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), e, Map.of()));
  }
}