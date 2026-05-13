package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UsernameAlreadyExistException extends UserException {

  public UsernameAlreadyExistException(String username) {
    super(ErrorCode.DUPLICATE_USERNAME, Map.of("username", username));
  }
}
