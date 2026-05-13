package com.sprint.mission.discodeit.exception.service.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserException;
import java.util.Map;

public class DupEmailException extends UserException {

  public DupEmailException(String email) {
    super(ErrorCode.DUPLICATE_EMAIL, Map.of("email", email));
  }
}
