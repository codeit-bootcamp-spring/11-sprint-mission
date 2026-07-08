package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class RefreshTokenInvalidException extends AuthException {

  private RefreshTokenInvalidException() {
    super(ErrorCode.REFRESH_TOKEN_INVALID);
  }

  public static RefreshTokenInvalidException invalid() {
    return new RefreshTokenInvalidException();
  }
}