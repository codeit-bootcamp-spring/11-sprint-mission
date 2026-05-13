package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class ChannelNotFoundException extends ChannelException {

  private ChannelNotFoundException() {
    super(ErrorCode.CHANNEL_NOT_FOUND);
  }

  public static ChannelNotFoundException withId(UUID channelId) {
    ChannelNotFoundException exception = new ChannelNotFoundException();
    exception.addDetail(ErrorKey.CHANNEL_ID, channelId);
    return exception;
  }

}
