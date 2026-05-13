package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    String code,
    String message,
    Map<String, Object> details,
    String exceptionType,
    int status
) {

  public static ErrorResponse from(DiscodeitException e) {
    ErrorCode errorCode = e.getErrorCode();
    return new ErrorResponse(
        e.getTimestamp(),
        errorCode.getCode(),
        errorCode.getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        errorCode.getHttpStatus().value()
    );
  }
}