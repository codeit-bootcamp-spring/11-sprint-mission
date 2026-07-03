package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class RefreshTokenInvalidException extends AuthException {

  public RefreshTokenInvalidException() {
    super(ErrorCode.REFRESH_TOKEN_INVALID);
  }
}