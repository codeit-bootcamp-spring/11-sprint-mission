package com.sprint.mission.discodeit.exception.service;

public class DiffPasswordException extends RuntimeException {
    public DiffPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}
