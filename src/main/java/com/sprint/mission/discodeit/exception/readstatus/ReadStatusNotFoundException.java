package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends ReadStatusException {

  private ReadStatusNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static ReadStatusNotFoundException withId(UUID readStatusId) {
    return new ReadStatusNotFoundException(
        ErrorCode.READ_STATUS_NOT_FOUND,
        Map.of(
            "readStatusId", readStatusId
        )
    );
  }
}