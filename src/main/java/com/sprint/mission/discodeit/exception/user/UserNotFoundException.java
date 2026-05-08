package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;

import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(UUID userId) {
        super(UserErrorCode.USER_NOT_FOUND, Map.of("searchedUserId", userId));
    }

    public UserNotFoundException(String username) {
        super(UserErrorCode.USER_NOT_FOUND, Map.of("searchedUsername", username));
    }
}
