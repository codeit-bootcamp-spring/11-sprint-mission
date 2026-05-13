package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class MessageNotFoundException extends MessageException {

  private MessageNotFoundException() {
    super(ErrorCode.MESSAGE_NOT_FOUND);
  }

  public static MessageNotFoundException withId(UUID messageId) {
    MessageNotFoundException exception = new MessageNotFoundException();
    exception.addDetail(ErrorKey.MESSAGE_ID, messageId);
    return exception;
  }
}
