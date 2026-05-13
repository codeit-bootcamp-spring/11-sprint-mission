package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

  private final Instant timestamp;
  private final String code;
  private final String message;
  private final Map<String, Object> details;
  private final String exceptionType;
  private final int status;

  /// 커스텀 예외
  public ErrorResponse(DiscodeitException exception) {
    this(
        Instant.now(),
        exception.getErrorCode().getCode(),
        exception.getErrorCode().getMessage(),
        exception.getDetails(),
        exception.getClass().getSimpleName(),
        exception.getErrorCode().getStatus().value()
    );
  }

  /// 외부 예외 - 유효성 검사
  public ErrorResponse(ErrorCode errorCode, Exception exception, Map<String, Object> details) {
    this(
        Instant.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        details != null ? details : new HashMap<>(),  // null 방지
        exception.getClass().getSimpleName(),
        errorCode.getStatus().value()
    );
  }

  /// 외부 예외 - detail 불필요
  public ErrorResponse(ErrorCode errorCode, Exception exception) {
    this(errorCode, exception, new HashMap<>());
  }

}