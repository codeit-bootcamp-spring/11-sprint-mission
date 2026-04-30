package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class PasswordNotMatchException extends AuthException {

  public PasswordNotMatchException(String username) {
    super(ErrorCode.PASSWORD_NOT_MATCH, Map.of("attemptedUsername", username));
  }
}
