package com.sprint.mission.discodeit.exception.user;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String name) {
        super("Name already exists: " + name);
    }
}
