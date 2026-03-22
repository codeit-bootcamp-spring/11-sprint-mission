package com.sprint.mission.discodeit.exception.user;


//가입 시 이미 사용 중인 userName이나 email일 때 409
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}