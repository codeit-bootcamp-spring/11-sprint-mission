package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class MessageContentEmptyException extends MessageException {

  public MessageContentEmptyException(UUID channelId, UUID userId) {
    super(ErrorCode.MESSAGE_CONTENT_EMPTY, Map.of("channelId", channelId, "userId", userId));
  }
}
