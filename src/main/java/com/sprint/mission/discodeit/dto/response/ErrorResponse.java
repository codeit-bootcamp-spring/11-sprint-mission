package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class ErrorResponse {

  private final Instant timestamp;
  private final String code;
  private final String message;
  private final Map<String, Object> details;

  // 발생한 예외의 클래스 이름
  private final String exceptionType;

  // HTTP 상태코드
  private final int status;

  private ErrorResponse(Instant timestamp, String code, String message, Map<String, Object> details,
      String exceptionType, int status) {
    this.timestamp = timestamp;
    this.code = code;
    this.message = message;
    this.details = details;
    this.exceptionType = exceptionType;
    this.status = status;
  }

  public static ErrorResponse of(ErrorCode errorCode, int status, Exception e) {
    return new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        Map.of(),
        e.getClass().getSimpleName(),
        status
    );
  }

  public static ErrorResponse of(ErrorCode errorCode, int status, Exception e,
      Map<String, Object> details) {
    return new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        details != null ? details : Map.of(),
        e.getClass().getSimpleName(),
        status
    );
  }
  
}
