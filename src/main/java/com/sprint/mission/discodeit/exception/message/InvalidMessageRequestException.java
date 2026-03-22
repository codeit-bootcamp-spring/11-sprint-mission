package com.sprint.mission.discodeit.exception.message;

//메시지 내용이 없거나 파라미터가 잘못되었을 때 400
public class InvalidMessageRequestException extends RuntimeException {
    public InvalidMessageRequestException(String message) {
        super(message);
    }
}