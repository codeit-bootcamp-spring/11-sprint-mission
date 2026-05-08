package com.sprint.mission.discodeit.exception.user;

import java.util.Map;

public class DuplicateUsernameException extends UserException{
    public DuplicateUsernameException(String username) {
        super(UserErrorCode.DUPLICATE_USERNAME, Map.of("searchedUsername", username));
    }
}
