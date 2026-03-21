package com.sprint.mission.discodeit.exception.user;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
