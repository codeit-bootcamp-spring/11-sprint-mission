package com.sprint.mission.discodeit.exception.service.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ReadStatusException;
import java.util.Map;
import java.util.UUID;


public class DupReadStatus extends ReadStatusException {

  public DupReadStatus(UUID userId, UUID channelId) {
    super(
        ErrorCode.DUPLICATE_READSTATUS,
        Map.of("UserId", userId, "ChannelId", channelId)
    );
  }
}
