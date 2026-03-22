package com.sprint.mission.discodeit.exception.userStatus;

//존재하지 않는 ID로 UserStatus를 조회, 수정, 삭제할 때 404
public class UserStatusNotFoundException extends RuntimeException {
    public UserStatusNotFoundException(String message) {
        super(message);
    }
}
