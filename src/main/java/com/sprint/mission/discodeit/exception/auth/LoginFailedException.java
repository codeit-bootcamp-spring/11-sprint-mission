package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class LoginFailedException extends AuthException {

  public LoginFailedException(String email) {
    super(ErrorCode.LOGIN_FAILED, Map.of("attemptedEmail", email));
  }
}
