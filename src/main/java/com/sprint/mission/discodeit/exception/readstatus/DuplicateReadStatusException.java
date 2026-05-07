package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class DuplicateReadStatusException extends ReadStatusException {

  private DuplicateReadStatusException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static DuplicateReadStatusException withUserAndChannel(UUID userId, UUID channelId) {
    return new DuplicateReadStatusException(
        ErrorCode.DUPLICATE_READ_STATUS,
        Map.of(
            "userId", userId,
            "channelId", channelId
        )
    );
  }
}