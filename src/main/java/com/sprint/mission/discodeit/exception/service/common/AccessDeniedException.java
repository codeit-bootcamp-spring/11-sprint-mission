package com.sprint.mission.discodeit.exception.service.common;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class AccessDeniedException extends DiscodeitException {

  public AccessDeniedException(String key, UUID value) {

    super(ErrorCode.ACCESS_DENIED, Map.of(key, value.toString()));
  }
}
