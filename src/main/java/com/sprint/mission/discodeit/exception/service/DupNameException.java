package com.sprint.mission.discodeit.exception.service;

public class DupNameException extends RuntimeException {
    public DupNameException() {
        super("중복되는 이름입니다.");
    }
}
