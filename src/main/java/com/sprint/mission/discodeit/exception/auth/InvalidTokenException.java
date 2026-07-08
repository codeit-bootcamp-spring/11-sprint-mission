package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidTokenException extends AuthException {

  private InvalidTokenException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static InvalidTokenException withToken(String token) {
    return new InvalidTokenException(
        ErrorCode.INVALID_TOKEN,
        Map.of("token", token)
    );
  }
}
