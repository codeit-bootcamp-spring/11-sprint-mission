package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.UserException;

import java.util.Map;

public class UsernameAlreadyExistsException extends UserException {
    public UsernameAlreadyExistsException(String username) {
        super(ErrorCode.USER_NAME_ALREADY_EXISTS, Map.of("username", username));
    }
}
