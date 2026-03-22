package com.sprint.mission.discodeit.exception.channel;


//필수 값(AdminID, MemberID 등)이 누락되었을 때 400
public class InvalidChannelRequestException extends RuntimeException {
    public InvalidChannelRequestException(String message) {
        super(message);
    }
}
