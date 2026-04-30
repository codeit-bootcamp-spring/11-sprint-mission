package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends MessageException {

  public ReadStatusNotFoundException(UUID userId, UUID messageId) {
    super(ErrorCode.READ_STATUS_NOT_FOUND, Map.of("userId", userId, "messageId", messageId));
  }
}
