package com.sprint.mission.discodeit.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 비즈니스 예외
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(DiscodeitException e) {
    log.warn("DiscodeitException 발생: {}", e.getMessage());
    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(new ErrorResponse(e));
  }

  // 유효성 검사 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    log.warn("MethodArgumentNotValidException 발생: 유효성 검사 실패");
    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

    Map<String, Object> details = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(error ->
        details.put(error.getField(), error.getDefaultMessage())
    );

    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e, details));
  }

  // 파일 용량 초과 예외
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  protected ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException e) {
    log.warn("MaxUploadSizeExceededException 발생: 파일 용량 초과");
    ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEEDED;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }


  // 파라미터 타입 불일치
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    log.warn("MethodArgumentTypeMismatchException 발생: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.INVALID_TYPE_VALUE;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }


  // 필수 파라미터 누락
  @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      org.springframework.web.bind.MissingServletRequestParameterException e) {
    log.warn("MissingServletRequestParameterException 발생: 파라미터 누락 - {}", e.getParameterName());
    ErrorCode errorCode = ErrorCode.MISSING_REQUEST_PARAMETER;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }


  // 잘못된 인자 값 전달
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("IllegalArgumentException 발생: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }


  // 지원하지 않는 HTTP 메서드 호출
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.warn("HttpRequestMethodNotSupportedException 발생: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }


  // 존재하지 않는 API 주소
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
    log.warn("NoResourceFoundException 발생: 잘못된 URL 요청 - {}", e.getResourcePath());
    ErrorCode errorCode = ErrorCode.API_NOT_FOUND;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }

  // 지원하지 않는 Content-Type
  @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
      org.springframework.web.HttpMediaTypeNotSupportedException e) {
    log.warn("HttpMediaTypeNotSupportedException 발생: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }


  // 직접 핸들링 하지 않은 모든 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Internal Server Error: 서버 내부 에러 발생", e);
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, e));
  }
}