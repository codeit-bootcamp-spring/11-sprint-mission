package com.sprint.mission.discodeit.exception;

public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다"),
    DUPLICATE_USER("이미 존재하는 사용자입니다"),
    INVALID_PASSWORD("비밀번호가 올바르지 않습니다"),

    CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다"),
    PRIVATE_CHANNEL_UPDATE("Private 채널은 수정할 수 없습니다"),

    MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다"),

    BINARY_CONTENT_NOT_FOUND("파일을 찾을 수 없습니다"),

    READ_STATUS_NOT_FOUND("읽음 상태를 찾을 수 없습니다"),

    USER_STATUS_NOT_FOUND("사용자 상태를 찾을 수 없습니다"),
    DUPLICATE_USER_STATUS("이미 존재하는 사용자 상태입니다"),

    VALIDATION_FAILED("입력값 유효성 검사에 실패했습니다");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}