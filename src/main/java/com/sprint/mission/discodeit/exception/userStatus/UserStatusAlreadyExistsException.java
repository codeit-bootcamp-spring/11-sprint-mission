package com.sprint.mission.discodeit.exception.userStatus;


//이미 상태 정보가 있는 유저에게 또 상태를 생성하려 할 때 409
public class UserStatusAlreadyExistsException extends RuntimeException {
    public UserStatusAlreadyExistsException(String message) {
        super(message);
    }
}
