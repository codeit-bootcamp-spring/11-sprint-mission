package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ChannelNotFoundException extends ChannelException {

  private ChannelNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static ChannelNotFoundException withId(UUID channelId) {
    return new ChannelNotFoundException(
        ErrorCode.CHANNEL_NOT_FOUND,
        Map.of(
            "channelId", channelId
        )
    );
  }
}