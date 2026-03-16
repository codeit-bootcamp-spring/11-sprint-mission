package com.sprint.mission.discodeit.exception.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User not found: "+ id);
    }

    public UserNotFoundException(String name) {
        super("User not found: "+ name);
    }
}
