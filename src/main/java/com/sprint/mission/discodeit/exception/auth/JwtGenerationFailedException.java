package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class JwtGenerationFailedException extends AuthException {

  public JwtGenerationFailedException() {
    super(ErrorCode.JWT_GENERATION_FAILED, Map.of());
  }
}
