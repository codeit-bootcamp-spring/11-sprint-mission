package com.sprint.mission.discodeit.exception.user;



//필수 값 누락되었을 때 400
public class InvalidUserRequestException extends RuntimeException {
    public InvalidUserRequestException(String message){
        super(message);
    }
}
