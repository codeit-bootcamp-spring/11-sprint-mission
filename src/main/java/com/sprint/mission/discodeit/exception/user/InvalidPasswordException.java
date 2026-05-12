package com.sprint.mission.discodeit.exception.user;

public class InvalidPasswordException extends UserException{
    public InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD, null);
    }
}
