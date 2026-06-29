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

  // 애플리케이션 내부에서 터뜨린 커스텀 예외 전용
  public static ErrorResponse from(DiscodeitException ex) {
    return new ErrorResponse(
        ex.getTimestamp(),
        ex.getErrorCode().name(),
        ex.getMessage(),
        ex.getDetails(),
        ex.getClass().getSimpleName(),
        ex.getErrorCode().getStatus().value()
    );
  }

  // 스프링이나 외부 라이브러리가 터뜨린 외부 예외 전용 (핸들러가 ErrorCode를 지정해서 넘겨줌)
  public static ErrorResponse of(ErrorCode errorCode, Exception ex, Map<String, Object> details) {
    return new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        details != null ? details : Map.of(),
        ex.getClass().getSimpleName(),
        errorCode.getStatus().value()
    );
  }
}
