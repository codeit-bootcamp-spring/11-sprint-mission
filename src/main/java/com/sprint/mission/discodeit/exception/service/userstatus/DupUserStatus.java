package com.sprint.mission.discodeit.exception.service.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ReadStatusException;
import com.sprint.mission.discodeit.exception.UserStatusException;
import java.util.Map;
import java.util.UUID;


public class DupUserStatus extends UserStatusException {

  public DupUserStatus(UUID userId) {
    super(
        ErrorCode.DUPLICATE_USERSTATUS,
        Map.of("UserId", userId)
    );
  }
}
