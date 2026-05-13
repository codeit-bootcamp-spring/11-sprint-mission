package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    int status,
    String exceptionType,
    String message,
    Map<String, Object> details) {

  public static ErrorResponse from(DiscodeitException ex) {
    return new ErrorResponse(
        ex.getErrorCode().getStatus().value(),
        ex.getClass().getSimpleName(),
        ex.getMessage(), ex.getDetails()
    );
  }
}
