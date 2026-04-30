package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReadStatusAlreadyExistsException extends MessageException {

  public ReadStatusAlreadyExistsException(UUID userId, UUID messageId) {
    super(ErrorCode.READ_STATUS_EXISTS, Map.of("userId", userId, "messageId", messageId));
  }
}
