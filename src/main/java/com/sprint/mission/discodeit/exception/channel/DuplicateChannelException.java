package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class DuplicateChannelException extends ChannelException {

  private DuplicateChannelException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static DuplicateChannelException withName(String name) {
    return new DuplicateChannelException(
        ErrorCode.DUPLICATE_CHANNEL,
        Map.of(
            "name", name
        )
    );
  }
}
