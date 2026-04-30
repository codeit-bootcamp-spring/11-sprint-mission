package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class PrivateChannelUpdateException extends ChannelException {

  public PrivateChannelUpdateException(UUID channelId) {
    super(ErrorCode.CHANNEL_MODIFY_PRIVATE, Map.of("channelId", channelId));
  }
}
