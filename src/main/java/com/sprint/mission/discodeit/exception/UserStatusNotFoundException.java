package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class UserStatusNotFoundException extends RuntimeException {
    public UserStatusNotFoundException(UUID id) {
        super("User Status not found of User " + id);
    }
}
