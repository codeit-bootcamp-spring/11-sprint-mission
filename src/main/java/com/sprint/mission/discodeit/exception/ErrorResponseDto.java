package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDto(
    int status,
    String exceptionType,
    String message,
    Map<String, Object> details,
    Instant timestamp
) {

  public static ErrorResponseDto of(int status, Exception e, Map<String, Object> details) {
    return new ErrorResponseDto(
        status,
        e.getClass().getSimpleName(),
        e.getMessage(),
        details,
        Instant.now()
    );
  }
}