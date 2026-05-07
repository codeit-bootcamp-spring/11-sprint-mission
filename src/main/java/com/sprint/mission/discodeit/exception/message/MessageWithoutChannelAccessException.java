package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class MessageWithoutChannelAccessException extends MessageException {

  private MessageWithoutChannelAccessException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static MessageWithoutChannelAccessException withUserAndChannel(UUID userId,
      UUID channelId) {
    return new MessageWithoutChannelAccessException(
        ErrorCode.MESSAGE_WITHOUT_CHANNEL_ACCESS,
        Map.of(
            "userId", userId,
            "channelId", channelId
        )
    );
  }
}
