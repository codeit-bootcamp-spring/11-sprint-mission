package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidUserDetailsException extends AuthException {

  private InvalidUserDetailsException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static InvalidUserDetailsException withInvalidPrincipal() {
    return new InvalidUserDetailsException(
        ErrorCode.INVALID_USER_DETAILS,
        Map.of()
    );
  }
}
