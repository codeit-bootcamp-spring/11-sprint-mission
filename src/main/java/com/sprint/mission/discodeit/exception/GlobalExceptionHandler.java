package com.sprint.mission.discodeit.exception;

import java.nio.file.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 1. 리소스가 없는 경우(사용자/채널/메시지/파일 등)
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNotFound(NotFoundException e) {
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", e.getMessage()),
        HttpStatus.NOT_FOUND
    );
  }

  // 2. 중복 리소스 생성
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponseDto> handleDuplicate(DuplicateResourceException e) {
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.CONFLICT.value(), "DUPLICATE_RESOURCE", e.getMessage()),
        HttpStatus.CONFLICT
    );
  }

  // 3. 인증관련(로그인 오류 등)
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDto> handleAuth(AuthenticationException e) {
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", e.getMessage()),
        HttpStatus.UNAUTHORIZED
    );
  }

  // 4. 권한 없음(비공개 채널 접근 등)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException e) {
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", e.getMessage()),
        HttpStatus.FORBIDDEN
    );
  }

  // 5. 파라미터 부족 등 잘못된 요청
  @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
  public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception e) {
    // 입력값 검증 실패시에 좀더 친절한 메시지로
    String msg = e.getMessage();
    if (e instanceof MethodArgumentNotValidException ex && ex.getBindingResult().hasErrors()) {
      msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    }
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", msg),
        HttpStatus.BAD_REQUEST
    );
  }

  // 6. 지원하지 않는 HTTP 메소드
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException e) {
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.METHOD_NOT_ALLOWED.value(), "METHOD_NOT_ALLOWED",
            e.getMessage()),
        HttpStatus.METHOD_NOT_ALLOWED
    );
  }

  // 7. 모든 예측 못한 서버 에러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
    return new ResponseEntity<>(
        new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR",
            e.getMessage()),
        HttpStatus.INTERNAL_SERVER_ERROR
    );
  }
}
