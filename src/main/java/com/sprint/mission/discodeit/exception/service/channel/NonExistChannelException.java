package com.sprint.mission.discodeit.exception.service.channel;

import com.sprint.mission.discodeit.exception.ChannelException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NonExistChannelException extends ChannelException {

  public NonExistChannelException(UUID channelId) {
    super(

        ErrorCode.CHANNEL_NOT_FOUND,
        Map.of("channelId", channelId)

    );
  }
}
