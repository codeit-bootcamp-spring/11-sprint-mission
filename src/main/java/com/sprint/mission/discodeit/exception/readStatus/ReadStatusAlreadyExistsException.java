package com.sprint.mission.discodeit.exception.readStatus;

// 이미 해당 유저-채널 읽음 상태가 있을 때 409
public class ReadStatusAlreadyExistsException extends RuntimeException {
    public ReadStatusAlreadyExistsException(String message) {
        super(message);
    }
}
