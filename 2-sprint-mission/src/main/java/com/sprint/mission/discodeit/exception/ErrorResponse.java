package com.sprint.mission.discodeit.exception;

import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.BindingResult;

public record ErrorResponse(
    String code,
    String message,
    List<FieldError> errors
) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), new ArrayList<>());
  }

  // 유효성 검사용
  public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
    return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(),
        FieldError.of(bindingResult));
  }

  public record FieldError(
      String field,
      String value,
      String reason
  ) {

    public static List<FieldError> of(BindingResult bindingResult) {
      return bindingResult.getFieldErrors().stream()
          .map(error -> new FieldError(
              error.getField(),
              error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
              error.getDefaultMessage()
          ))
          .toList();
    }
  }
}