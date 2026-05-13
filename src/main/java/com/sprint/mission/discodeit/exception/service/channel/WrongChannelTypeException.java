package com.sprint.mission.discodeit.exception.service.channel;

import com.sprint.mission.discodeit.exception.ChannelException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class WrongChannelTypeException extends ChannelException {

  public WrongChannelTypeException(UUID channelId) {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE, Map.of("channelId", channelId));
  }
}
