package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UnauthorizedException extends AuthException {

  private UnauthorizedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static UnauthorizedException withAuthenticationRequired() {
    return new UnauthorizedException(ErrorCode.UNAUTHORIZED, Map.of());
  }
}
