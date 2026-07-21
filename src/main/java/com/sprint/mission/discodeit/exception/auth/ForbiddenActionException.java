package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class ForbiddenActionException extends AuthException {

  public ForbiddenActionException(String reason) {
    super(ErrorCode.ACCESS_DENIED, Map.of("reason", reason));
  }
}
