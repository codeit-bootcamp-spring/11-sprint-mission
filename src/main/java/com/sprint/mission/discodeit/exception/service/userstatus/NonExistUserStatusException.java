package com.sprint.mission.discodeit.exception.service.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserStatusException;
import java.util.Map;
import java.util.UUID;

public class NonExistUserStatusException extends UserStatusException {

  public NonExistUserStatusException(UUID userStatusId) {
    super(
        ErrorCode.USERSTATUS_NOT_FOUND,
        Map.of("userStatusId", userStatusId + "")

    );
  }
}
