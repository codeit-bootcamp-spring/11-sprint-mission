package com.sprint.mission.discodeit.exception.service;

public class DupEmailException extends RuntimeException {
    public DupEmailException()
    {
        super("중복되는 이메일 입니다.");
    }
}
