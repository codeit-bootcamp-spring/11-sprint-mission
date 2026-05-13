package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class InsufficientParticipantsException extends ChannelException {

  public InsufficientParticipantsException() {
    super(ErrorCode.INSUFFICIENT_PARTICIPANTS, null);
  }
}
