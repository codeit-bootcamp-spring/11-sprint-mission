package com.sprint.mission.discodeit.exception.service.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ReadStatusException;
import java.util.Map;
import java.util.UUID;

public class NonExistReadStatusException extends ReadStatusException {

  public NonExistReadStatusException(UUID userId, UUID channelId) {
    super(ErrorCode.READSTATUS_NOT_FOUND, Map.of("userId", userId, "channelId", channelId));
  }

  public NonExistReadStatusException(UUID readStatusId) {
    super(ErrorCode.READSTATUS_NOT_FOUND, Map.of("readStatusId", readStatusId));
  }
}
