package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserStatusAlreadyExists extends UserStatusException {

  public UserStatusAlreadyExists(UUID userId) {
    super(ErrorCode.USER_ALREADY_EXISTS, Map.of("userId", userId));
  }
}
