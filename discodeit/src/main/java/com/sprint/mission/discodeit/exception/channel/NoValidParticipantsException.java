package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NoValidParticipantsException extends ChannelException {

  private NoValidParticipantsException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static NoValidParticipantsException withRequestedIds(List<UUID> requestedIds) {
    return new NoValidParticipantsException(
        ErrorCode.NO_VALID_PARTICIPANTS,
        Map.of(
            "requestedIds", requestedIds
        )
    );
  }
}
