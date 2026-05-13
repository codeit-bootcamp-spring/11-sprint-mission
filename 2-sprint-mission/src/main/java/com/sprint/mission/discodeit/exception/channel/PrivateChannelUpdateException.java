package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class PrivateChannelUpdateException extends ChannelException {

  private PrivateChannelUpdateException() {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED);
  }

  public static PrivateChannelUpdateException forChannel(UUID channelId) {
    PrivateChannelUpdateException exception = new PrivateChannelUpdateException();
    exception.addDetail(ErrorKey.CHANNEL_ID, channelId);
    return exception;
  }
}