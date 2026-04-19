package com.sprint.mission.discodeit.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

public record ErrorResponse(
    String code,
    String exceptionType,
    String message,
    @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL) List<String> fields
) {

  public static ErrorResponse from(ApiException.ERROR errorCode) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getExceptionType(),
        errorCode.getMessage(),
        null
    );
  }

  public static ErrorResponse from(ApiException.ERROR errorCode, List<String> fields) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getExceptionType(),
        errorCode.getMessage(),
        fields
    );
  }
}