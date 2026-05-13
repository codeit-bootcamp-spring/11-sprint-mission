package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class DiscodeitException extends RuntimeException {

  private final Instant timestamp;
  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public DiscodeitException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = details != null ? details : Map.of();
    this.timestamp = Instant.now();
  }

  public DiscodeitException(ErrorCode errorCode) {
    this(errorCode, Map.of());
  }
}
