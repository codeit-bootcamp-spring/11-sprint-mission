package com.sprint.mission.discodeit.exception;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.COMMON_UNEXPECTED_ERROR;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(ApiException e) {
    return ResponseEntity
        .status(e.getError().getHttpStatus())
        .body(ErrorResponse.from(e.getError()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.from(COMMON_UNEXPECTED_ERROR));
  }
}
