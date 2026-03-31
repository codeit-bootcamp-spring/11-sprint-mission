package com.sprint.mission.discodeit.advice;

import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.InputMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessLogicException.class)
  public ResponseEntity<ErrorResponse> handleBusinessLogicException(BusinessLogicException e) {
    ExceptionCode exceptionCode = e.getExceptionCode();

    ErrorResponse response = ErrorResponse.of(exceptionCode);

    return new ResponseEntity<>(response, HttpStatus.valueOf(exceptionCode.getStatus()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
    e.printStackTrace();
    ErrorResponse response = ErrorResponse.of(ExceptionCode.INTERNAL_SERVER_ERROR);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
