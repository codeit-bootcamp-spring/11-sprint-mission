package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class AccessTokenInvalidException extends AuthException {

  public AccessTokenInvalidException() {
    super(ErrorCode.ACCESS_TOKEN_INVALID);
  }
}