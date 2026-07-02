package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class ForbiddenException extends AuthException {

  private ForbiddenException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static ForbiddenException withAccessDenied() {
    return new ForbiddenException(ErrorCode.FORBIDDEN, Map.of());
  }
}
