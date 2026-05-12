package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.AuthException;

import java.util.Map;

public class InvalidPasswordException extends AuthException {
    public InvalidPasswordException(String username) {
        super(ErrorCode.AUTH_INVALID_PASSWORD, Map.of("username", username));
    }
}
