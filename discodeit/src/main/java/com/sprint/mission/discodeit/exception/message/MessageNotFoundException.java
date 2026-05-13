package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class MessageNotFoundException extends MessageException {

  private MessageNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static MessageNotFoundException withId(UUID messageId) {
    return new MessageNotFoundException(
        ErrorCode.MESSAGE_NOT_FOUND,
        Map.of(
            "messageId", messageId
        )
    );
  }
}
