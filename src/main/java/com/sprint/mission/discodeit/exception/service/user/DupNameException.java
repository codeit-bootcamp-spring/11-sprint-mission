package com.sprint.mission.discodeit.exception.service.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class DupNameException extends DiscodeitException {

  public DupNameException(String name) {
    super(ErrorCode.DUPLICATE_USER, Map.of("name", name));
  }
}
