package com.sprint.mission.discodeit.exception.service.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.MessageException;
import java.util.Map;
import java.util.UUID;

public class NonExistMessageException extends MessageException {

  public NonExistMessageException(UUID messageId) {
    super(
        ErrorCode.MESSAGE_NOT_FOUND,
        Map.of("messageId", messageId + "")

    );
  }
}
