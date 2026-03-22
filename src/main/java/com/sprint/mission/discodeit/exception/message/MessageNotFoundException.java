package com.sprint.mission.discodeit.exception.message;

//존재하지 않는 ID로 메시지를 조회, 수정, 삭제할 때 404
public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String message) {
        super(message);
    }
}