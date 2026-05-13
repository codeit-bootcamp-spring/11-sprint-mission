package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.sprint.mission.discodeit.controller")
// 모든 컨트롤러에서 발생하는 예외를 가로채는 역할
public class GlobalExceptionHandler {

  // 유효성 검증 예외
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e
  ) {

    Map<String, Object> details = new HashMap<>();

    // 상세 오류 메시지
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      details.put(error.getField(), error.getDefaultMessage());
    }

    ErrorResponse response = ErrorResponse.of(
        ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(), e, details);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 비즈니스 로직 예외
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    HttpStatus status = mapToStatus(e.getErrorCode());

    ErrorResponse response = ErrorResponse.of(e.getErrorCode(), status.value(), e, e.getDetails());

    return ResponseEntity.status(status).body(response);
  }

  // IllegalArgumentException 불법인수, 잘못된 인수(잘못된 요청 ?) / bad request / 400
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    ErrorResponse response = ErrorResponse.of(
        ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(), e,
        Map.of("reason", e.getMessage()));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 추가 : 개발자마저 인지하지 못하는 예외가 있을 수 있기 때문에 예외 최상위 클래스 Exception 예외를 추가, 500번(예외 발생, 서버 코드 문제, 개발자 실수)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleOtherException(Exception e) {
    ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.value(), e, Map.of("reason", e.getMessage()));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  private HttpStatus mapToStatus(ErrorCode errorCode) {
    return switch (errorCode) {

      // 400
      case INVALID_REQUEST, PRIVATE_CHANNEL_UPDATE -> HttpStatus.BAD_REQUEST;

      // 401
      case INVALID_LOGIN -> HttpStatus.UNAUTHORIZED;

      // 404
      case USER_NOT_FOUND, CHANNEL_NOT_FOUND, MESSAGE_NOT_FOUND,
           USER_STATUS_NOT_FOUND, READ_STATUS_NOT_FOUND, BINARY_CONTENT_NOT_FOUND ->
          HttpStatus.NOT_FOUND;

      // 409
      case DUPLICATE_USERNAME, DUPLICATE_EMAIL, USER_STATUS_ALREADY_EXISTS -> HttpStatus.CONFLICT;

      // 500
      default -> HttpStatus.INTERNAL_SERVER_ERROR;

    };
  }

}
