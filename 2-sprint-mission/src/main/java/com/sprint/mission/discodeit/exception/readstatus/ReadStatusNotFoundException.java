package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class ReadStatusNotFoundException extends ReadStatusException {

  private ReadStatusNotFoundException() {
    super(ErrorCode.READ_STATUS_NOT_FOUND);
  }

  public static ReadStatusNotFoundException withId(UUID readStatusId) {
    ReadStatusNotFoundException exception = new ReadStatusNotFoundException();
    exception.addDetail(ErrorKey.READ_STATUS_ID, readStatusId);
    return exception;
  }
}
