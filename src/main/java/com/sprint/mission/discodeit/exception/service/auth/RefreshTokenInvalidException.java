package com.sprint.mission.discodeit.exception.service.auth;

import com.sprint.mission.discodeit.exception.AuthException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class RefreshTokenInvalidException extends AuthException {

  public RefreshTokenInvalidException() {
    super(ErrorCode.REFRESH_TOKEN_INVALID, Map.of());
  }
}

