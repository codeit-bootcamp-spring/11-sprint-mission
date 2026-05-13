package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class ChannelException extends DiscodeitException {

  public ChannelException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public ChannelException(ErrorCode errorCode) {
    super(errorCode);
  }
}