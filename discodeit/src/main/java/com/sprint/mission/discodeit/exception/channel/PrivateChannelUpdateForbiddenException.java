package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class PrivateChannelUpdateForbiddenException extends ChannelException {

  private PrivateChannelUpdateForbiddenException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static PrivateChannelUpdateForbiddenException withId(UUID channelId) {
    return new PrivateChannelUpdateForbiddenException(
        ErrorCode.PRIVATE_CHANNEL_UPDATE_FORBIDDEN,
        Map.of(
            "channelId", channelId
        )
    );
  }
}
