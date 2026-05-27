package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class ErrorResponse {

  private final Instant timestamp;
  private final String code;
  private final String message;
  private final Map<String, Object> details;
  private final String exceptionType;
  private final int status;

  public ErrorResponse(Instant timestamp, String code, String message, Map<String, Object> details,
      String exceptionType, int status) {
    this.timestamp = timestamp;
    this.code = code;
    this.message = message;
    this.details = details;
    this.exceptionType = exceptionType;
    this.status = status;
  }

  public ErrorResponse(Exception e, int status) {
    if (e instanceof DiscodeitException de) {
      this.timestamp = de.getTimestamp();
      this.code = de.getErrorCode().name();
      this.message = de.getMessage();
      this.details = de.getDetails();
    } else {
      this.timestamp = Instant.now();
      this.code = "INTERNAL_SERVER_ERROR";
      this.message = e.getMessage();
      this.details = new HashMap<>();
    }
    this.exceptionType = e.getClass().getSimpleName();
    this.status = status;
  }
}
