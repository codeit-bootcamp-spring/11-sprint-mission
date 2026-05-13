package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidCredentialsException extends AuthException {

  private InvalidCredentialsException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static InvalidCredentialsException withWrongPassword() {
    return new InvalidCredentialsException(
        ErrorCode.INVALID_CREDENTIALS,
        Map.of()
    );
  }
}