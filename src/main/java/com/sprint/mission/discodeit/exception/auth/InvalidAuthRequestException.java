package com.sprint.mission.discodeit.exception.auth;

//아이디나 비밀번호가 빈 값으로 넘어왔을 때 400
public class InvalidAuthRequestException extends RuntimeException {
    public InvalidAuthRequestException(String message) { super(message); }
}
