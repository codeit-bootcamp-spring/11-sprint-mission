package com.sprint.mission.discodeit.exception.auth;

//아이디가 없거나, 비밀번호가 틀렸을 때 401
public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message) { super(message); }
}
