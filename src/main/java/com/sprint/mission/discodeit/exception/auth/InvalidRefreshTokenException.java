package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidRefreshTokenException extends AuthException {

  private InvalidRefreshTokenException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static InvalidRefreshTokenException withToken(String token) {
    return new InvalidRefreshTokenException(
        ErrorCode.INVALID_REFRESH_TOKEN,
        Map.of("token", token)
    );
  }
}