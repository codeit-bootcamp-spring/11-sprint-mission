package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class BadCredentialsException extends AuthException {

  public BadCredentialsException(String username) {
    super(ErrorCode.AUTHENTICATION_FAILED, Map.of("username", username));
  }
}
