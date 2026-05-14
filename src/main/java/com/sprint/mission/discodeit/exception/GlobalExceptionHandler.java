package com.sprint.mission.discodeit.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 커스텀 예외
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<String> handleDiscodeitException(DiscodeitException e) {
    log.warn("DiscodeitException 발생: {}", e.getMessage());
    ErrorResponse response = new ErrorResponse(
        e.getErrorCode().getStatus(),
        e.getClass().getSimpleName(),
        e.getMessage(),
        e.getDetails()
    );
    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(e.getMessage());
  }

  // Validation 실패 예외
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException e) {
    log.warn("ValidationException 발생: {}", e.getMessage());
    ErrorResponse response = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        e.getClass().getSimpleName(),
        ErrorCode.INVALID_INPUT_VALUE.getMessage(),
        e.getBindingResult().getAllErrors()
    );
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    e.printStackTrace();
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(e.getMessage());
  }
}
