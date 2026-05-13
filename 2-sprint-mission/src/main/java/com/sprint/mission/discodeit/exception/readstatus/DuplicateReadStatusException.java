package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class DuplicateReadStatusException extends ReadStatusException {

  private DuplicateReadStatusException() {
    super(ErrorCode.DUPLICATE_READ_STATUS);
  }

  public static DuplicateReadStatusException withUserIdAndChannelId(UUID userId, UUID channelId) {
    DuplicateReadStatusException exception = new DuplicateReadStatusException();
    exception.addDetail(ErrorKey.USER_ID, userId);
    exception.addDetail(ErrorKey.CHANNEL_ID, channelId);
    return exception;
  }
}