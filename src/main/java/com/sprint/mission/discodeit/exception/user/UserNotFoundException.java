package com.sprint.mission.discodeit.exception.user;


//존재하지 않는 ID로 유저를 조회, 수정, 삭제할 때 404
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
