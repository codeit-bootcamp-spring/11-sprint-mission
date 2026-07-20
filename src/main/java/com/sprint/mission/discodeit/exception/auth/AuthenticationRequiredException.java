package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class AuthenticationRequiredException extends AuthException {

  public AuthenticationRequiredException() {
    super(ErrorCode.AUTHENTICATION_REQUIRED);
  }
}