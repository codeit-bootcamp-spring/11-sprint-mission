package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 만든 모든 커스텀 예외를 여기서 한 방에 처리 가능
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handle(DiscodeitException ex) {
    log.warn("예외 발생 - {}: {}", ex.getClass().getSimpleName(), ex.getMessage());

    return ResponseEntity
        .status(ex.getErrorCode().getStatus())
        .body(ErrorResponse.from(ex));
  }

  // DTO에 올바르지 않은 필드값이 들어올 경우 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    log.error("예외 발생 - {}: 입력값 유효성 검사 실패", ex.getClass().getSimpleName());

    // 필드명을 Key로, 에러 메시지를 Value로 하는 Map 객체를 details 담아줌
    Map<String, Object> details = ex.getBindingResult().getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값",
            (a, b) -> a
        ));

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "VALIDATION_FAILED",
        "입력값 유효성 검사에 실패했습니다.",
        details,
        ex.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.badRequest()
        .body(errorResponse);
  }

  // 그 외 예상치 못한 모든 런타임 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
    log.error("예상치 못한 예외", ex);

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "INTERNAL_SERVER_ERROR",
        "서버 내부에서 예상치 못한 오류가 발생했습니다.",
        Map.of(), // 숨겨야 하는 서버 내부 에러 정보는 details에 담지 않고 빈 맵으로 넘김
        ex.getClass().getSimpleName(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }
}